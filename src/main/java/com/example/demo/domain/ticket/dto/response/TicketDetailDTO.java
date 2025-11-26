package com.example.demo.domain.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailDTO {

    private Long ticketId;
    private String concertTitle;
    private LocalDateTime concertDateTime;
    private String buyerName;
    private LocalDate birth;
    private String ticketNumber;
    private String seatNumber;
    private String NftUrl;
    private boolean isEntered;
    private int price;
    private Boolean isResaleListed;
    private String photoCardUrl;
    private Long sessionId;
    
}
