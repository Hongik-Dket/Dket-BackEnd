package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.lottery.service.LotteryService;
import com.example.demo.domain.metadata.service.MetadataService;
import com.example.demo.domain.ownership.service.OwnershipService;
import com.example.demo.domain.resale.service.ResaleService;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.global.infra.blockchain.contracts.DketNFT;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketNFTListenService {

    private final Web3j web3j;
    private final Credentials credentials;

    private final MetadataService metadataService;
    private final DketNFTViewService dketNFTViewService;
    private final TicketService ticketService;
    private final LotteryService lotteryService;
    private final ResaleService resaleService;
    private final OwnershipService ownershipService;

    @Value("${web3.resale-contract-address}")
    private String resaleContractAddress;

    private final DketNFT dketNFT;

    @PostConstruct
    public void init() {

        listenToRandomFulfilled();
        listenToSessionMinted();
        listenToApplicantsListCommitted();
        listenToWinnersDrawn();
        listenToSetDrawn();
        listenToPaymentTransferred();
        listenToApproval();
    }

    private void listenToRandomFulfilled() {
        dketNFT.randomFulfilledEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            BigInteger sessionId = event.sessionId;
                            BigInteger randomWord = event.randomWord;

                            log.info("randomFulfilled: session [{}]", sessionId);

                            List<Long> metadataIds = metadataService.createMetadata(sessionId, randomWord);
                            metadataService.uploadAllMetadataAsync(metadataIds);
                        },
                        error -> {
                            log.error("randomFulfilled 이벤트 수신 중 에러 발생", error);
                        }
                );
    }

    private void listenToSessionMinted() {
        dketNFT.sessionMintedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            List<BigInteger> tokenIds = event.tokenIds;

                            if (tokenIds == null || tokenIds.isEmpty()) {
                                throw new CustomException(ErrorStatus.SESSION_MINTING_FAILED);
                            }

                            log.info("sessionMinted: tokenIds [{}]", tokenIds);

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

    private void listenToApplicantsListCommitted() {
        dketNFT.applicantsListCommittedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            Long sessionId = event.sessionId.longValue();

                            log.info("applicantsListCommitted: session [{}]", sessionId);

                            lotteryService.drawWinners(sessionId);
                        },
                        error -> {
                            log.error("applicantsListCommitted 이벤트 수신 중 예외 발생", error);
                        }
                );
    }

    private void listenToWinnersDrawn() {
        dketNFT.winnersDrawnEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            Long sessionId = event.sessionId.longValue();
                            int count = event.count.intValue();

                            String txHash = event.log.getTransactionHash();
                            long blockNumber = event.log.getBlockNumber().longValue();
                            int logIndex = event.log.getLogIndex().intValue();

                            log.info("winnersDrawn: session[{}]", sessionId);

                            lotteryService.saveWinners(
                                    sessionId, count, event.winnerIdx, txHash, blockNumber, logIndex
                            );
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

                            log.info("setDrawn: session [{}]", sessionId);

                            lotteryService.completeDraw(sessionId);
                        },
                        error -> {
                            log.error("setDrawn 이벤트 수신 중 예외 발생", error);
                        }
                );
    }

    private void listenToPaymentTransferred() {
        dketNFT.paymentTransferredEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            String buyer = event.to;
                            Long sessionId = event.sessionId.longValue();
                            BigInteger tokenId = event.tokenId;

                            log.info("paymentTransferred: session[{}], token[{}]", sessionId, tokenId);

                            ticketService.completeTicket(buyer, sessionId, tokenId);

                            String txHash = event.log.getTransactionHash();
                            long blockNumber = event.log.getBlockNumber().longValue();
                            int logIndex = event.log.getLogIndex().intValue();

                            ownershipService.createOwnership(buyer, sessionId, tokenId, txHash, blockNumber, logIndex);
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

                            log.info("approval: owner [{}]", event.owner);

                            resaleService.listResale(event.owner, event.tokenId);
                        },
                        error -> {
                            log.error("Approval 이벤트 수신 중 예외 발생", error);
                        }
                );
    }

}
