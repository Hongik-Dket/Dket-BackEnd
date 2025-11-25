package com.example.demo.global.infra.blockchain.service.impl;

import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.lottery.entity.ApplicantsSnapshot;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.global.infra.blockchain.contracts.DketNFT;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.demo.global.util.Hexes.hexToBytes;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketNFTServiceImpl implements DketNFTService {

    private final SessionRepository sessionRepository;

    private final DketNFT dketNFT;

    @Override
    public String recordConcertOnChain(Concert concert, List<Session> sessionList) {
        try {
            ZoneId zone = ZoneId.of("Asia/Seoul");
            LocalTime startTime = concert.getStartTime();

            List<BigInteger> sessionIds = new ArrayList<>();
            List<BigInteger> startAts = new ArrayList<>();

            for (Session session : sessionList) {
                sessionIds.add(BigInteger.valueOf(session.getId()));

                long startAt = session.getDate()
                        .atTime(startTime)
                        .atZone(zone)
                        .toEpochSecond();

                startAts.add(BigInteger.valueOf(startAt));
            }

            log.info("Sending DketNFT.createConcert: concertId={}", concert.getId());

            var tx = dketNFT.createConcert(
                    BigInteger.valueOf(concert.getId()),
                    concert.getOrganizer().getWalletAddress(),
                    concert.getTitle(),
                    BigInteger.valueOf(concert.getCapacity()),
                    concert.getPriceWei(),
                    concert.getIsResaleAllowed(),
                    sessionIds,
                    startAts
            ).send();

            log.info("Completed DketNFT.createConcert: concertId={}", concert.getId());

            return tx.getTransactionHash();
        } catch (Exception e) {
            log.error("Concert [{}] 온체인 기록 실패", concert.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    @Override
    public void mintSessionTicket(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        List<String> metadataUris = session.getMetadataList().stream()
                .map(metadata -> "ipfs://" + metadata.getCid())
                .collect(Collectors.toList());

        log.info("Sending DketNFT.mintSessionTicket: sessionId={}", session.getId());
        try {
            dketNFT.mintSessionTicket(
                    BigInteger.valueOf(session.getId()),
                    metadataUris
            ).send();
        } catch (Exception e) {
            log.error("Session [{}] mintSessionTicket 실패", session.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketNFT.mintSessionTicket: sessionId={}", session.getId());
    }

    @Override
    public void setApplicantsListCommitment(Session session, ApplicantsSnapshot snapshot) {
            log.info("Sending DketNFT.setApplicantsListCommitment: sessionId={}", session.getId());
        try {
            dketNFT.setApplicantsListCommitment(
                    BigInteger.valueOf(session.getId()),
                    hexToBytes(snapshot.getListHash()),
                    BigInteger.valueOf(Integer.toUnsignedLong(snapshot.getTotalCount()))
            ).send();
        } catch (Exception e) {
            log.error("Session [{}] setApplicantsListCommitment 실패", session.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketNFT.setApplicantsListCommitment: sessionId={}", session.getId());
    }

    @Override
    public void drawWinnersOnChain(Session session, int count, List<byte[]> leaves) {
            log.info("Sending DketNFT.drawWinners: sessionId={}", session.getId());
        try {
            dketNFT.drawWinners(
                    BigInteger.valueOf(session.getId()),
                    BigInteger.valueOf(Integer.toUnsignedLong(count)),
                    leaves
            ).send();
        } catch (Exception e) {
            log.error("Session [{}] drawWinners 실패", session.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketNFT.drawWinners: sessionId={}", session.getId());
    }

    @Override
    public void finalizeWinnersRoot(Session session) {
            log.info("Sending DketNFT.finalizeWinnersRoot: sessionId={}, winnersRoot={}",
                    session.getId(), session.getWinnersRoot());
        try {
            dketNFT.finalizeWinnersRoot(
                    BigInteger.valueOf(session.getId()),
                    session.getWinnersRoot()
            ).send();
        } catch (Exception e) {
            log.error("Session [{}] finalizeWinnersRoot 실패", session.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketNFT.finalizeWinnersRoot: sessionId={}, winnersRoot={}",
                session.getId(), session.getWinnersRoot());
    }

    @Override
    public void setDrawnOnChain(Session session) {
            log.info("Sending DketNFT.setDrawn: sessionId={}", session.getId());
        try {
            dketNFT.setDrawn(BigInteger.valueOf(session.getId())).send();
        } catch (Exception e) {
            log.error("Session [{}] setDrawn 실패", session.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketNFT.setDrawn: sessionId={}", session.getId());
    }

    @Override
    public void openPublicSaleOnChain(Concert concert) {
            log.info("Sending DketNFT.openPublicSale: concertId={}", concert.getId());
        try {
            dketNFT.openPublicSale(BigInteger.valueOf(concert.getId())).send();
        } catch (Exception e) {
            log.error("Concert [{}] 온체인 선착순 판매 전환 실패", concert.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketNFT.openPublicSale: concertId={}", concert.getId());
    }

    @Override
    public void updateOwnersRoot(Session session) {
            log.info("Sending DketNFT.updateOwnersRoot: sessionId={}, ownersRoot={}",
                    session.getId(), session.getOwnersRoot());
        try {
            dketNFT.updateOwnersRoot(
                    BigInteger.valueOf(session.getId()),
                    session.getOwnersRoot()
            ).send();
        } catch (Exception e) {
            log.error("Session [{}] updateOwnersRoot 실패", session.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketNFT.updateOwnersRoot: sessionId={}, ownersRoot={}",
                session.getId(), session.getOwnersRoot());
    }

    @Override
    public void enterTicketOnChain(Ticket ticket, List<BigInteger> proof, byte[] nullifier) {
            log.info("Sending DketNFT.enter: ticketId={}", ticket.getId());
        try {
            dketNFT.enter(
                    BigInteger.valueOf(ticket.getSession().getId()),
                    ticket.getTokenId(),
                    proof,
                    nullifier
            ).send();
        } catch (Exception e) {
            log.error("Ticket [{}] 입장 실패", ticket.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketNFT.enter: ticketId={}", ticket.getId());
    }

}