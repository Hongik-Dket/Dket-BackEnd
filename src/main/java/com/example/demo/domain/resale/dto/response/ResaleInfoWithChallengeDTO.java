package com.example.demo.domain.resale.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResaleInfoWithChallengeDTO {

    private Long resaleId;
    private BigInteger tokenId;
    private String challengeId;
    private String challenge;
}
