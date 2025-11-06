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
public class ResaleInfoDTO {

    private Long resaleId;
    private BigInteger tokenId;
}
