package com.example.demo.domain.resale.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResaleDetailDTO {

    private Long resaleId;
    private String concertTitle;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private String seatCode;
    private int originalPrice;
    private int priceKrw;
    private BigInteger priceWei;
    private String photoCardUrl;

}
