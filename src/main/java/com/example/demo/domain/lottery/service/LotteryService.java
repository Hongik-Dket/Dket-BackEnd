package com.example.demo.domain.lottery.service;

import java.math.BigInteger;
import java.util.List;

public interface LotteryService {

    void saveWinners(
            Long sessionId, int count, List<BigInteger> indices, String txHash, Long blockNo, Integer logIdx);

    void completeDraw(Long sessionId);

}
