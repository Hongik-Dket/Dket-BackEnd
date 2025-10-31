package com.example.demo.domain.ticket.service;

import com.example.demo.domain.ticket.dto.request.EntryCodeDTO;
import com.example.demo.domain.ticket.dto.response.EntryProofDataDTO;
import com.example.demo.domain.ticket.dto.response.TicketDetailDTO;

public interface BuyerTicketService {

    TicketDetailDTO getTicketDetail(Long ticketId);

    EntryProofDataDTO getEntryProofData(Long ticketId, EntryCodeDTO request);

}
