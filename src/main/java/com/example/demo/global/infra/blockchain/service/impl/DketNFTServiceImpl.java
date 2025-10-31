package com.example.demo.global.infra.blockchain.service.impl;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.concert.service.SessionService;
import com.example.demo.domain.metadata.service.MetadataService;
import com.example.demo.domain.resale.service.ResaleService;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.global.event.ReadyToMintEvent;
import com.example.demo.global.infra.blockchain.contracts.DketNFT;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.infra.blockchain.service.DketNFTViewService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketNFTServiceImpl implements DketNFTService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final MetadataService metadataService;
    private final SessionRepository sessionRepository;
    private final TicketService ticketService;
    private final SessionService sessionService;
    private final DketNFTViewService dketNFTViewService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ResaleService resaleService;


    @Value("${web3.nft-contract-address}")
    private String nftContractAddress;

    @Value("${web3.resale-contract-address}")
    private String resaleContractAddress;

    private DketNFT dketNFT;

    @PostConstruct
    public void init() {
        dketNFT = DketNFT.load(
                nftContractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );

        listenToRandomFulfilled();
        listenToWinnersDrawn();
        listenToSetDrawn();
        listenToSessionMinted();
        listenToPaymentTransferred();
        listenToApproval();
    }

    @Override
    public String recordConcertOnChain(Concert concert) {
        try {
            var tx = dketNFT.createConcert(
                    BigInteger.valueOf(concert.getId()),
                    concert.getOrganizer().getWalletAddress(),
                    concert.getTitle(),
                    BigInteger.valueOf(concert.getCapacity()),
                    concert.getPriceWei(),
                    concert.getIsResaleAllowed()
            ).send();

            return tx.getTransactionHash();
        } catch (Exception e) {
            log.error("Concert [{}] 온체인 기록 실패", concert.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    @Override
    public String recordSessionOnChain(Session session) {
        try {
            List<String> applications = session.getApplyList().stream()
                    .map(apply -> apply.getUser().getWalletAddress())
                    .collect(Collectors.toList());

            ZoneId zone = ZoneId.of("Asia/Seoul");
            long startAt = session.getDate()
                    .atTime(session.getConcert().getStartTime())
                    .atZone(zone)
                    .toEpochSecond();

            var tx = dketNFT.createSession(
                    BigInteger.valueOf(session.getConcert().getId()),
                    BigInteger.valueOf(session.getId()),
                    BigInteger.valueOf(startAt),
                    applications
            ).send();

            return tx.getTransactionHash();
        } catch (Exception e) {
            log.error("Session [{}] 온체인 기록 실패", session.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    @Override
    public void openPublicSaleOnChain(Concert concert) {
        try {
            dketNFT.openPublicSale(BigInteger.valueOf(concert.getId())).send();
        } catch (Exception e) {
            log.error("Concert [{}] 온체인 선착순 판매 전환 실패", concert.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    @Override
    public void enterTicketOnChain(Ticket ticket) {
        try {
            dketNFT.enter(ticket.getTokenId()).send();
        } catch (Exception e) {
            log.error("Ticket [{}] 입장 실패", ticket.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    private void listenToRandomFulfilled() {
        dketNFT.randomFulfilledEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
            .subscribe(
                event -> {
                    BigInteger sessionId = event.sessionId;
                    BigInteger randomWord = event.randomWord;

                    List<Long> metadataIds = metadataService.createMetadata(sessionId, randomWord);
                    metadataService.uploadAllMetadataAsync(metadataIds);

                    scheduler.schedule(() -> {
                        try {
                            drawSession(sessionId);
                        } catch (Exception e) {
                            log.error("Session [{}] 추첨 지연 실패", sessionId, e);
                        }
                    }, 3, TimeUnit.SECONDS);
                },
                error -> {
                    log.error("randomFulfilled 이벤트 수신 중 에러 발생", error);
                }
            );
    }

    @Transactional
    protected void drawSession(BigInteger sessionId) {
        List<Type> input = List.of(new Uint256(sessionId));
        sendTransaction("drawSession", input);
    }

    private void listenToWinnersDrawn() {
        dketNFT.winnersDrawnEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
            .subscribe(
                event -> {
                    BigInteger sessionId = event.sessionId;
                    List<String> winners = event.winners;

                    if (winners == null || winners.isEmpty()) {
                        throw new CustomException(ErrorStatus.SESSION_DRAW_FAILED);
                    }

                    sessionService.saveWinners(sessionId.longValue(), winners);
                },
                error -> {
                    log.error("winnersDrawn 이벤트 수신 중 예외 발생", error);
                }
            );
    }

    private void listenToSetDrawn() {
        dketNFT.setDrawnEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
            .subscribe(
                event -> {
                    Long sessionId = event.sessionId.longValue();
                    sessionService.completeDraw(sessionId);
                },
                error -> {
                    log.error("setDrawn 이벤트 수신 중 예외 발생", error);
                }
            );
    }

    @EventListener
    public void handleMetadataUploaded(ReadyToMintEvent event) {
        Session session = sessionRepository.findById(event.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        mintSessionTicket(session);
    }

    private void mintSessionTicket(Session session) {
        List<String> metadataUris = session.getMetadataList().stream()
                .map(metadata -> "ipfs://" + metadata.getCid())
                .collect(Collectors.toList());

        BigInteger sessionId = BigInteger.valueOf(session.getId());

        List<Type> input = Arrays.asList(
                new Uint256(sessionId),
                new DynamicArray<>(
                    Utf8String.class,
                    metadataUris.stream()
                        .map(Utf8String::new)
                        .collect(Collectors.toList())
                )
        );
        sendTransaction("mintSessionTicket", input);
    }

    private void listenToSessionMinted() {
        dketNFT.sessionMintedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
            .subscribe(
                event -> {
                    List<BigInteger> tokenIds = event.tokenIds;

                    if (tokenIds == null || tokenIds.isEmpty()) {
                        throw new CustomException(ErrorStatus.SESSION_MINTING_FAILED);
                    }

                    registerMintedTickets(tokenIds);
                },
                error -> {
                    log.error("sessionMinted 이벤트 수신 중 예외 발생", error);
                }
            );
    }

    private void registerMintedTickets(List<BigInteger> tokenIds) {
        List<String> cidList = new ArrayList<>();

        for (BigInteger tokenId : tokenIds) {
            cidList.add(dketNFTViewService.getTokenUri(tokenId).replace("ipfs://", ""));
        }

        ticketService.batchRegisterTicket(tokenIds, cidList);
    }

    private void listenToPaymentTransferred() {
        dketNFT.paymentTransferredEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
            .subscribe(
                event -> {
                    String buyer = event.to;
                    Long sessionId = event.sessionId.longValue();
                    Long tokenId = event.tokenId.longValue();

                    ticketService.completeTicket(buyer, sessionId, tokenId);
                },
                error -> {
                    log.error("paymentTransferred 이벤트 수신 중 예외 발생", error);
                }
            );
    }

    private void listenToApproval() {
        dketNFT.approvalEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            String approved = event.approved;
                            if (!approved.equalsIgnoreCase(resaleContractAddress)) return;

                            resaleService.listResale(event.owner, event.tokenId);
                        },
                        error -> {
                            log.error("Approval 이벤트 수신 중 예외 발생", error);
                        }
                );
    }

    private void sendTransaction(String functionName, List<Type> inputParams) {
        try {
            Function function = new Function(functionName, inputParams, Collections.emptyList());
            String encodedFunction = FunctionEncoder.encode(function);
            BigInteger gasLimit = estimateGas(encodedFunction);
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

            RawTransactionManager txManager = new RawTransactionManager(web3j, credentials);
            EthSendTransaction response = txManager.sendTransaction(
                    gasPrice,
                    gasLimit,
                    dketNFT.getContractAddress(),
                    encodedFunction,
                    BigInteger.ZERO
            );

            if (response.hasError()) {
                log.error("{} 트랜잭션 실패: {}", functionName, response.getError().getMessage());
                throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
            }
        } catch (Exception e) {
            log.error("{} 실패", functionName, e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    private BigInteger estimateGas(String encodedFunction) {
        try {
            Transaction ethCallTransaction = Transaction.createEthCallTransaction(
                    credentials.getAddress(),
                    dketNFT.getContractAddress(),
                    encodedFunction
            );

            EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(ethCallTransaction).send();

            if (ethEstimateGas.hasError()) {
                Response.Error error = ethEstimateGas.getError();
                log.error("Gas 추정 실패 - Code: {}, Message: {}, Data: {}",
                        error.getCode(), error.getMessage(), error.getData());
                throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
            }

            return ethEstimateGas.getAmountUsed().multiply(BigInteger.valueOf(120)).divide(BigInteger.valueOf(100));

        } catch (Exception e) {
            log.error("Gas 추정 중 예외 발생", e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
        }
    }
}