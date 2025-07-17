package com.example.demo.domain.main.dto;

import com.example.demo.domain.concert.enums.ConcertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertCardDTO {

    private Long concertId;
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private ConcertStatus concertStatus;
    private String imageUrl;

}
