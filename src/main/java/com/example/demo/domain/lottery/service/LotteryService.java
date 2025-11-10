package com.example.demo.domain.lottery.service;

import com.example.demo.domain.concert.entity.Session;

import java.math.BigInteger;
import java.util.List;

public interface LotteryService {

    void commitApplicants(Session session);

    void drawWinners(Long sessionId);

    void saveWinners(
            Long sessionId, int count, List<BigInteger> indices, String txHash, Long blockNo, Integer logIdx);

    void completeDraw(Long sessionId);

}
