package com.example.demo.domain.ticket.converter;

import com.example.demo.domain.ticket.dto.TicketDTO;
import com.example.demo.domain.ticket.dto.TicketDetailDTO;
import com.example.demo.domain.ticket.entity.Ticket;

import java.time.LocalDateTime;

public class TicketConverter {

    public static TicketDetailDTO toTicketDetailDTO(Ticket ticket, String NftUrl) {
        LocalDateTime eventDateTime = LocalDateTime.of(
                ticket.getSession().getDate(),
                ticket.getSession().getEvent().getStartTime()
        );

        return TicketDetailDTO.builder()
                .ticketId(ticket.getId())
                .eventTitle(ticket.getSession().getEvent().getTitle())
                .eventDateTime(eventDateTime)
                .buyerName(ticket.getUser().getName())
                .birth(ticket.getUser().getBirth())
                .ticketNumber(ticket.getMetadata().getTicketNumber())
                .seatNumber(ticket.getMetadata().getSeatCode())
                .qrCodeUrl(ticket.getQrCode())
                .photoCardId(ticket.getMetadata().getPhotoCard().getId())
                .NftUrl(NftUrl)
                .isEntered(ticket.getEnteredAt() != null)
                .build();
    }

    public static TicketDTO toTicketDTO(Ticket ticket) {
        return TicketDTO.builder()
                .ticketId(ticket.getId())
                .eventTitle(ticket.getSession().getEvent().getTitle())
                .posterUrl(ticket.getSession().getEvent().getPosterUrl())
                .location(ticket.getSession().getEvent().getLocation())
                .sessionDate(ticket.getSession().getDate())
                .startTime(ticket.getSession().getEvent().getStartTime())
                .entered(ticket.getEnteredAt() != null)
                .build();
    }
}
