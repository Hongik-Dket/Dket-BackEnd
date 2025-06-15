package com.example.demo.domain.ticket.dto;

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
public class TicketDTO {

    private Long ticketId;
    private String eventTitle;
    private LocalDateTime eventDateTime;
    private String buyerName;
    private LocalDate birth;
    private String ticketNumber;
    private String seatNumber;
    private String qrCodeUrl;
    private Long photoCardId;
    private String NFTUrl;
    private boolean isEntered;
    
    
}
