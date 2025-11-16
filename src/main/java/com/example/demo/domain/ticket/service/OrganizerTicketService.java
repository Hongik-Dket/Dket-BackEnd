package com.example.demo.domain.ticket.service;

import com.example.demo.domain.ticket.dto.request.ProofRequestDTO;
import com.example.demo.domain.ticket.dto.response.IdentityTypeDTO;
import com.example.demo.domain.ticket.dto.response.TicketResponseDTO;

public interface OrganizerTicketService {

    void enterTicket(Long ticketId);

    TicketResponseDTO validateTicketWithoutProof(String ticketNumber);

    IdentityTypeDTO verifyOwnProofAndEnter(ProofRequestDTO request);

}
