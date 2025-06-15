package com.example.demo.domain.ticket.converter;

import com.example.demo.domain.ticket.dto.TicketDTO;
import com.example.demo.domain.ticket.dto.TicketDetailDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.global.base.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TicketConverter {

    @Value("${web3.contract-address}")
    private String contractAddress;

    public TicketDetailDTO toTicketDetailDTO(Ticket ticket) {
        LocalDateTime eventDateTime = LocalDateTime.of(
                ticket.getSession().getDate(),
                ticket.getSession().getEvent().getStartTime()
        );

        String NFTUrl = Constants.OPENSEA_BASE_URL + contractAddress + "/%s".formatted(ticket.getTokenId());

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
                .NFTUrl(NFTUrl)
                .isEntered(ticket.getEnteredAt() != null)
                .build();
    }

    public TicketDTO toTicketDTO(Ticket ticket) {
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
