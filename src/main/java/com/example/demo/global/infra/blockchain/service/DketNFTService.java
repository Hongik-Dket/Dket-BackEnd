package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;

public interface DketNFTService {
    String recordEventOnChain(Event event);

    String recordSessionOnChain(Session session);

    void openPublicSaleOnChain(Event event);

}