package com.example.demo.domain.ticket.service;

import com.example.demo.domain.ticket.dto.TicketResponseDTO;
import com.example.demo.domain.user.entity.User;

public interface TicketService {

    TicketResponseDTO getTicket(Long ticketId, User currentUser);

}
