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
public class ResaleAuthDTO {

    private BigInteger tokenId;
    private BigInteger expireAt;
    private String signature;

}
