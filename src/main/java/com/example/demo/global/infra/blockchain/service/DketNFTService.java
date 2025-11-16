package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.lottery.entity.ApplicantsSnapshot;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.ticket.entity.Ticket;

import java.math.BigInteger;
import java.util.List;

public interface DketNFTService {
    String recordConcertOnChain(Concert concert , List<Session> sessionList);

    void mintSessionTicket(Long sessionId);

    void setApplicantsListCommitment(Session session, ApplicantsSnapshot snapshot);

    void drawWinnersOnChain(Session session, int count, List<byte[]> leaves);

    void finalizeWinnersRoot(Session session);

    void setDrawnOnChain(Session session);

    void openPublicSaleOnChain(Concert concert);

    void updateOwnersRoot(Session session);

    void enterTicketOnChain(Ticket ticket, List<BigInteger> proof, byte[] nullifier);

}