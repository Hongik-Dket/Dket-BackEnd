package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.apply.entity.ApplicantsSnapshot;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.ticket.entity.Ticket;
import org.web3j.abi.datatypes.generated.Bytes32;

import java.util.List;

public interface DketNFTService {
    String recordConcertOnChain(Concert concert , List<Session> sessionList);

    void setApplicantsListCommitment(Session session, ApplicantsSnapshot snapshot);

    void drawWinnersOnChain(Session session, int count, List<byte[]> leaves);

    void setDrawnOnChain(Session session);

    void openPublicSaleOnChain(Concert concert);

    void enterTicketOnChain(Ticket ticket);

}