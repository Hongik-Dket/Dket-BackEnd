package com.example.demo.global.infra.blockchain.dto;

import lombok.*;

import java.math.BigInteger;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateEventResponseDTO {
    private boolean isSuccess;
    private String code;
    private String message;
    private Result result;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private String txHash;
        private BigInteger priceWei;
    }
}
