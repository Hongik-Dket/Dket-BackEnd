package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;

public interface DketNFTService {
    String recordConcertOnChain(Concert concert);

    String recordSessionOnChain(Session session);

    void openPublicSaleOnChain(Concert concert);

}