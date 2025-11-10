package com.example.demo.domain.lottery.service;

import com.example.demo.domain.concert.entity.Session;

public interface LotteryOnChainService {

    void commitApplicants(Session session);

    void drawWinners(Long sessionId);

}
