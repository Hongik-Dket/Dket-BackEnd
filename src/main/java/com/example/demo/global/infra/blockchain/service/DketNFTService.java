package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.ticket.entity.Ticket;

import java.util.List;

public interface DketNFTService {
    String recordConcertOnChain(Concert concert , List<Session> sessionList);

    String recordSessionOnChain(Session session);

    void openPublicSaleOnChain(Concert concert);

    void enterTicketOnChain(Ticket ticket);

}