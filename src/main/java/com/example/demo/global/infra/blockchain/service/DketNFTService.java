package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.event.entity.Event;

public interface DketNFTService {
    String recordEventOnChain(Event event);

    void recordAllSessionsOnChain(Event event);

    void openPublicSaleOnChain(Event event);

}