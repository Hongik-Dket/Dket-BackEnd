package com.example.demo.domain.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    private Long ticketId;
    private String eventTitle;
    private String location;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private boolean entered;
}
