package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.service.SessionService;
import com.example.demo.global.infra.blockchain.contracts.DketNFT;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketNFTService {

    private final Web3j web3j;
    private final Credentials credentials;

    @Value("${web3.contract-address}")
    private String contractAddress;

    private DketNFT dketNFT;

    private final SessionService sessionService;

    @PostConstruct
    public void init() {
        dketNFT = DketNFT.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );

        listenToRandomFulfilled();
    }

    public String recordEventOnChain(Event event) {
        try {
            // Todo: IPFS 업로드 구현 후 수정 필요
            List<String> photoCardList = event.getPhotoCards().stream()
                    .map(photoCard -> photoCard.getIpfsUrl())
                    .collect(Collectors.toList());

            List<String> tmp = new ArrayList<>(List.of("test"));

            var tx = dketNFT.createEvent(
                    BigInteger.valueOf(event.getId()),
                    event.getOrganizer().getWalletAddress(),
                    event.getTitle(),
                    BigInteger.valueOf(event.getCapacity()),
                    event.getPriceWei(),
                    tmp
            ).send();

            return tx.getTransactionHash();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    @Transactional
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
                            drawSession(sessionId);
                        },
                        error -> {
                            System.out.println(error.getMessage());
                            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
                        }
                );
    }

    private void drawSession(BigInteger sessionId) {
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

            String txHash = txResponse.getTransactionHash();

            TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(web3j, 1000, 60); // 최대 15초 대기
            TransactionReceipt txReceipt = receiptProcessor.waitForTransactionReceipt(txHash);

            List<DketNFT.WinnersDrawnEventResponse> events = dketNFT.getWinnersDrawnEvents(txReceipt);

            if (!events.isEmpty() && events.get(0).winners != null) {
                List<String> winners = events.get(0).winners;
                sessionService.saveWinners(sessionId.longValue(), winners);
            } else
                throw new CustomException(ErrorStatus.BLOCKCHAIN_WINNERS_NOT_FOUND);

        } catch (Exception e) {
            System.out.println(e.getMessage());
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
                System.out.println(ethEstimateGas.getError().getMessage());
                throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
            }

            return ethEstimateGas.getAmountUsed().multiply(BigInteger.valueOf(120)).divide(BigInteger.valueOf(100));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
        }
    }
}