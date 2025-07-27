package com.example.demo.domain.concert.dto.request;

import com.example.demo.domain.concert.enums.AgeLimit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertUploadDTO {

    private String title;
    private AgeLimit ageLimit;
    private String location;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int priceKrw;
    private int capacity;
    private LocalDateTime applyStart;
    private LocalDateTime applyEnd;

}
