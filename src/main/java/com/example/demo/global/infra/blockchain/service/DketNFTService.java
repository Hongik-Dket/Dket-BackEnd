package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.ticket.entity.Ticket;

public interface DketNFTService {
    String recordConcertOnChain(Concert concert);

    String recordSessionOnChain(Session session);

    void openPublicSaleOnChain(Concert concert);

    void enterTicketOnChain(Ticket ticket);

}