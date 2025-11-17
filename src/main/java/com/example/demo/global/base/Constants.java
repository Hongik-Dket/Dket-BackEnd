package com.example.demo.global.base;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static com.example.demo.global.util.Keccak.keccak256;

public class Constants {

    // 응모 마감 이후 결제 가능 기한(일 단위): 마감일 제외 2일 후 자정
    public static final int PAYMENT_DEADLINE = 3;

    // 공연 시작 전 결제 가능 기한(시간 단위): 공연 시작 2시간 전까지 결제 가능
    public static final int PAYMENT_AVAILABLE_BEFORE_CONCERT_START = 2;

    // Etherscan NFT base url
    public static final String ETHERSCAN_NFT_BASE_URL = "https://eth-sepolia.blockscout.com/token/";

    // 리세일 티켓 예약 만료 시간(분 단위)
    public static final int RESALE_RESERVATION_EXPIRATION_MINUTES = 5;

    // 리세일 판매가 상한 비율 (원가 × 1.2)
    public static final BigDecimal RESALE_PRICE_LIMIT_RATE = BigDecimal.valueOf(1.20);

    // 온체인 기록 대기 시간(분 단위)
    public static final int ONCHAIN_TIMEOUT = 10;

    // DOMAIN_TAG = keccak256("ICv1")
    public static final byte[] DOMAIN_TAG_HASH = keccak256("ICv1".getBytes(StandardCharsets.UTF_8));

    // Challenge 만료 시간(초 단위)
    public static final int CHALLENGE_EXPIRATION_MINUTES = 10;

    // keccak256("Dket:apply") mod BN254
    public static final BigInteger APPLY_TAG = new BigInteger(
            "21893c20f71039c08143fce9ce8cc2246209d41942edd38ff7e003737b20a7a1", 16);

    // keccak256("Dket:own") mod BN254
    public static final BigInteger OWN_TAG = new BigInteger(
            "0b1ad30a3769ffdbb6a73732c1599ee47f8fcdb4c3fd7b39a51fd63183e0c4c7", 16);

}
