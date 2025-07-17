package com.example.demo.domain.concert.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerSessionInfoDTO {

    private Long concertId;
    private Long sessionId;
    private LocalDate date;
    private int applyCount;
    private int paidCount;
    private int attendeeCount;

}
