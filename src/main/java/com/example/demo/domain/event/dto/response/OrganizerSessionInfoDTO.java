package com.example.demo.domain.event.dto.response;

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

    private Long eventId;
    private Long sessionId;
    private LocalDate date;
    private int applyCount;
    private int paidCount;
    private int attendeeCount;

}
