package com.example.demo.domain.ticket.service;

import java.math.BigInteger;
import java.util.List;

public interface TicketService {
    void batchRegisterTicket(List<BigInteger> tokenIdList, List<String> cidList);

    void completeTicket(String address, Long sessionId, BigInteger tokenId);

}
