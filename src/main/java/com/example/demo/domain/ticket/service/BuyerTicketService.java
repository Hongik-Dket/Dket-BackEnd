package com.example.demo.domain.ticket.service;

import com.example.demo.domain.ticket.dto.TicketDetailDTO;

public interface BuyerTicketService {

    TicketDetailDTO getTicketDetail(Long ticketId);

}
