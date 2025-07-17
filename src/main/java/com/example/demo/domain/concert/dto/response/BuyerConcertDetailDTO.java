package com.example.demo.domain.concert.dto.response;

import com.example.demo.domain.concert.enums.AgeLimit;
import com.example.demo.domain.concert.enums.ConcertStatus;
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
public class BuyerConcertDetailDTO {
    private Long concertId;
    private String title;
    private String description;
    private String location;
    private AgeLimit ageLimit;
    private int priceKrw;

    private LocalDateTime applyStart;
    private LocalDateTime applyEnd;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private String posterUrl;
    private int capacity;

    private ConcertStatus concertStatus;

    private List<BuyerSessionInfoDTO> sessionList;
}
