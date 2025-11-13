package com.example.demo.domain.concert.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceWeiAndChallengeDTO {

    Long sessionId;
    BigInteger priceWei;
    String challengeId;
    String challenge;
}
