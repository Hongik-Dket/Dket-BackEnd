package com.example.demo.domain.ticket.service;

import com.example.demo.domain.ticket.dto.TicketResponseDTO;

public interface OrganizerTicketService {

    void enterTicket(Long ticketId);

    TicketResponseDTO validateTicketWithoutProof(String ticketNumber);

}
