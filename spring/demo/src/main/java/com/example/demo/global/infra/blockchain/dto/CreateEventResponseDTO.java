package com.example.demo.global.infra.blockchain.dto;

import lombok.*;

import java.math.BigInteger;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateEventResponseDTO {
    boolean isSuccess;
    String code;
    String message;
    Result result;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        String txHash;
        BigInteger priceWei;
    }
}
