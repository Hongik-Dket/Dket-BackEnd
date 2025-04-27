package com.example.demo.domain.main.dto;

import com.example.demo.domain.event.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCardDTO {

    private Long eventId;
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private EventStatus eventStatus;
    private String imageUrl;

}
