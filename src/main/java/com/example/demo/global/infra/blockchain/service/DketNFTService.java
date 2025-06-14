package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.event.service.SessionService;
import com.example.demo.domain.metadata.service.MetadataService;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.global.event.ReadyToMintEvent;
import com.example.demo.global.infra.blockchain.contracts.DketNFT;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
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
import org.web3j.tx.exceptions.ContractCallException;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketNFTService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final MetadataService metadataService;
    private final SessionRepository sessionRepository;
    private final TicketService ticketService;
    private final SessionService sessionService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${web3.contract-address}")
    private String contractAddress;

    private DketNFT dketNFT;

    @PostConstruct
    public void init() {
        dketNFT = DketNFT.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );

        listenToRandomFulfilled();
        listenToWinnersDrawn();
        listenToSessionMinted();
        listenToPaymentTransferred();
    }

    public String recordEventOnChain(Event event) {
        try {
            var tx = dketNFT.createEvent(
                    BigInteger.valueOf(event.getId()),
                    event.getOrganizer().getWalletAddress(),
                    event.getTitle(),
                    BigInteger.valueOf(event.getCapacity()),
                    event.getPriceWei()
            ).send();

            return tx.getTransactionHash();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    public void recordAllSessionsOnChain(Event event) {
        event.getSessions().parallelStream().forEach(session -> {
            session.setTxHash(recordSessionOnChain(session));
        });
    }

    private String recordSessionOnChain(Session session) {
        try {
            List<String> applications = session.getApplyList().stream()
                    .map(apply -> apply.getUser().getWalletAddress())
                    .collect(Collectors.toList());

            var tx = dketNFT.createSession(
                    BigInteger.valueOf(session.getEvent().getId()),
                    BigInteger.valueOf(session.getId()),
                    applications
            ).send();

            return tx.getTransactionHash();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    public void openPublicSaleOnChain(Event event) {
        try {
            dketNFT.openPublicSale(BigInteger.valueOf(event.getId())).send();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    private void listenToRandomFulfilled() {
        dketNFT.randomFulfilledEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            BigInteger sessionId = event.sessionId;
                            BigInteger randomWord = event.randomWord;
                            drawSession(sessionId);
                            metadataService.createMetadata(sessionId, randomWord);
                        },
                        error -> {
                            System.out.println(error.getMessage());
                        }
                );
    }

    @Transactional
    protected void drawSession(BigInteger sessionId) {
        try {
            Function function = new Function(
                    "drawSession",
                    List.of(new Uint256(sessionId)),
                    List.of()
            );
            String encodedFunction = FunctionEncoder.encode(function);

            BigInteger gasLimit = estimateGas(encodedFunction);
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

            RawTransactionManager txManager = new RawTransactionManager(web3j, credentials);

            EthSendTransaction txResponse = txManager.sendTransaction(
                    gasPrice,
                    gasLimit,
                    dketNFT.getContractAddress(),
                    encodedFunction,
                    BigInteger.ZERO
            );

            if (txResponse.hasError()) {
                System.out.println(txResponse.getError().getMessage());
                throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    private void listenToWinnersDrawn() {
        dketNFT.winnersDrawnEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            BigInteger sessionId = event.sessionId;
                            List<String> winners = event.winners;

                            if (winners == null || winners.isEmpty())
                                throw new CustomException(ErrorStatus.SESSION_DRAW_FAILED);

                            sessionService.saveWinners(sessionId.longValue(), winners);
                            publishEventHandler(sessionId.longValue());
                        },
                        error -> {
                            System.out.println(error.getMessage());
                        }
                );
    }

    private void publishEventHandler(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        if (session.getMetadataUploaded())
            eventPublisher.publishEvent(new ReadyToMintEvent(sessionId));
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

        try {
            Function function = new Function(
                "mintSessionTicket",
                Arrays.asList(
                    new Uint256(sessionId),
                    new DynamicArray<>(
                        Utf8String.class,
                        metadataUris.stream()
                            .map(Utf8String::new)
                            .collect(Collectors.toList())
                    )
                ),
                Collections.emptyList()
            );
            String encodedFunction = FunctionEncoder.encode(function);

            BigInteger gasLimit = estimateGas(encodedFunction);
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

            RawTransactionManager txManager = new RawTransactionManager(web3j, credentials);

            EthSendTransaction txResponse = txManager.sendTransaction(
                    gasPrice,
                    gasLimit,
                    dketNFT.getContractAddress(),
                    encodedFunction,
                    BigInteger.ZERO
            );

            if (txResponse.hasError()) {
                System.out.println(txResponse.getError().getMessage());
                throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    private void listenToSessionMinted() {
        dketNFT.sessionMintedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            List<BigInteger> tokenIds = event.tokenIds;

                            if (tokenIds == null || tokenIds.isEmpty())
                                throw new CustomException(ErrorStatus.SESSION_MINTING_FAILED);

                            registerMintedTickets(tokenIds);
                        },
                        error -> {
                            System.out.println(error.getMessage());
                        }
                );
    }

    private void registerMintedTickets(List<BigInteger> tokenIds) {
        List<String> cidList = new ArrayList<>();

        for (BigInteger tokenId : tokenIds)
            cidList.add(getTokenUri(tokenId).replace("ipfs://", ""));

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
                            System.out.println(error.getMessage());
                        }
                );
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
                System.out.println("EstimateGas Error: " + error.getMessage());
                System.out.println("Code: " + error.getCode());
                System.out.println("Data: " + error.getData());
                throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
            }

            return ethEstimateGas.getAmountUsed().multiply(BigInteger.valueOf(120)).divide(BigInteger.valueOf(100));

        } catch (Exception e) {
//            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
        }
    }

    private String getTokenUri(BigInteger tokenId) {
        try {
            return dketNFT.tokenURI(tokenId).send();
        } catch (ContractCallException e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.TOKEN_INVALID);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
        }
    }
}