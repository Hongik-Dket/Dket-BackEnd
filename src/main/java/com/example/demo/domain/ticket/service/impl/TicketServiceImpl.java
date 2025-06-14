package com.example.demo.domain.ticket.service.impl;

import com.example.demo.domain.ticket.dto.TicketResponseDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.response.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public TicketResponseDTO getTicket(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User owner = ticket.getOwner();

        TicketResponseDTO.TicketResponseDTOBuilder builder = TicketResponseDTO.builder()
                .ticketId(ticket.getId())
                .eventTitle(ticket.getSession().getEvent().getTitle())
                .eventDateTime(LocalDateTime.of(
                        ticket.getSession().getDate(),
                        ticket.getSession().getEvent().getStartTime()
                ))
                .buyerName(owner.getName())
                .birth(owner.getBirth())
                .ticketNumber(ticket.getTicketNumber())
                .seatNumber(ticket.getSeatNumber());

        if (owner.getId().equals(currentUser.getId())) {
            builder.qrCodeUrl(ticket.getQrCodeUrl());
        }

        return builder.build();
    }
}
