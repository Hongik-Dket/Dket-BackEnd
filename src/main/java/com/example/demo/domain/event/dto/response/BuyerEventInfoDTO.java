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
public class BuyerEventInfoDTO {
    private Long eventId;
    private String title;
    private String description;
    private String location;
    private AgeLimit ageLimit;
    private int price; // priceKrw 그대로 가져와도 됨

    private LocalDateTime applyStart;
    private LocalDateTime applyEnd;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private String posterUrl;
    private int capacity;

    private EventStatus eventStatus;

    private List<BuyerSessionDTO> sessionList;
}
