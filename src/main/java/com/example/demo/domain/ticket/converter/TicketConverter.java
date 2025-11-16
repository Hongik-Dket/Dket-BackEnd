package com.example.demo.domain.ticket.converter;

import com.example.demo.domain.ticket.dto.TicketCardDTO;
import com.example.demo.domain.ticket.dto.TicketDetailDTO;
import com.example.demo.domain.ticket.entity.Ticket;

import java.time.LocalDateTime;

public class TicketConverter {

    public static TicketDetailDTO toTicketDetailDTO(Ticket ticket, String NftUrl, Boolean isResaleListed) {
        LocalDateTime concertDateTime = LocalDateTime.of(
                ticket.getSession().getDate(),
                ticket.getSession().getConcert().getStartTime()
        );

        return TicketDetailDTO.builder()
                .ticketId(ticket.getId())
                .concertTitle(ticket.getSession().getConcert().getTitle())
                .concertDateTime(concertDateTime)
                .buyerName(ticket.getUser().getName())
                .birth(ticket.getUser().getBirth())
                .ticketNumber(ticket.getMetadata().getTicketNumber())
                .seatNumber(ticket.getMetadata().getSeatCode())
                .NftUrl(NftUrl)
                .isEntered(ticket.getEnteredAt() != null)
                .price(ticket.getSession().getConcert().getPriceKrw())
                .isResaleListed(isResaleListed)
                .photoCardUrl(ticket.getMetadata().getPhotoCard().getUrl())
                .build();
    }

    public static TicketCardDTO toTicketDTO(Ticket ticket) {
        return TicketCardDTO.builder()
                .ticketId(ticket.getId())
                .concertTitle(ticket.getSession().getConcert().getTitle())
                .posterUrl(ticket.getSession().getConcert().getPosterUrl())
                .location(ticket.getSession().getConcert().getLocation())
                .sessionDate(ticket.getSession().getDate())
                .startTime(ticket.getSession().getConcert().getStartTime())
                .entered(ticket.getEnteredAt() != null)
                .build();
    }
}
