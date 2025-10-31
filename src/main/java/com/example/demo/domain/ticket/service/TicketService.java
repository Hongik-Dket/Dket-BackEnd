package com.example.demo.domain.ticket.service;

import com.example.demo.domain.ticket.dto.TicketDetailDTO;
import com.example.demo.domain.ticket.entity.Ticket;

import java.math.BigInteger;
import java.util.List;

public interface TicketService {
    void batchRegisterTicket(List<BigInteger> tokenIdList, List<String> cidList);

    void completeTicket(String address, Long sessionId, Long tokenId);

    TicketDetailDTO getTicketDetail(Long ticketId);

    TicketDetailDTO getTicketByNumber(String ticketNumber);

    void enterTicket(Long ticketId);

    String getNftUrl(Ticket ticket);
}
