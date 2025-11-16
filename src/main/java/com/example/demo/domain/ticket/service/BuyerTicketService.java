package com.example.demo.domain.ticket.service;

import com.example.demo.domain.ticket.dto.response.TicketDetailDTO;

public interface BuyerTicketService {

    TicketDetailDTO getTicketDetail(Long ticketId);

}
