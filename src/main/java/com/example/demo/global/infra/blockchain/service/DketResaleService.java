package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.resale.entity.Resale;

import java.util.List;

public interface DketResaleService {

    void listResaleOnChain(Resale resale);

    void cancelResaleBatch(List<Long> resaleIds);

}
