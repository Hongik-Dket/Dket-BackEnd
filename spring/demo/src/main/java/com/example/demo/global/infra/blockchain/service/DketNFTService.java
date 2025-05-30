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
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tx.gas.DefaultGasProvider;

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

        listenToWinnersDrawn();
    }

    public String recordEventOnChain(Event event) {
        try {
            // Todo: 스마트 컨트랙트 수정 후 반영 필요
            List<BigInteger> photoCardIds = event.getPhotoCards().stream()
                    .map(photoCard -> BigInteger.valueOf(photoCard.getId()))
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
        for (Session session : event.getSessions())
            session.setTxHash(recordSessionOnChain(session));
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

    private void listenToWinnersDrawn() {
        dketNFT.winnersDrawnEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    Long sessionId = Long.valueOf(String.valueOf(event.sessionId));
                    List<String> winners = event.winners;

                    sessionService.saveWinners(sessionId, winners);
                });
    }

}