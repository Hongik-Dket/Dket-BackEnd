package com.example.demo.domain.event.dto.response;

import com.example.demo.domain.event.enums.AgeLimit;
import com.example.demo.domain.event.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInfoDTO {

    private Long eventId;
    private String title;
    private String posterUrl;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AgeLimit ageLimit;
    private int priceKrw;
    private LocalDateTime applyStart;
    private LocalDateTime applyEnd;
    private int capacity;
    private EventStatus eventStatus;
    private List<Long> sessionIds;

}
