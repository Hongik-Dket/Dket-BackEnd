package com.example.demo.global.base;

import java.math.BigDecimal;

public class Constants {

    // 응모 마감 이후 결제 가능 기한(일 단위): 마감일 제외 2일 후 자정
    public static final int PAYMENT_DEADLINE = 3;

    // 공연 시작 전 결제 가능 기한(시간 단위): 공연 시작 2시간 전까지 결제 가능
    public static final int PAYMENT_AVAILABLE_BEFORE_CONCERT_START = 2;

    // Etherscan NFT base url
    public static final String ETHERSCAN_NFT_BASE_URL = "https://sepolia.etherscan.io/nft/";

    // 리세일 티켓 예약 만료 시간(분 단위)
    public static final int RESALE_RESERVATION_EXPIRATION_MINUTES = 5;

    // 리세일 판매가 상한 비율 (원가 × 1.2)
    public static final BigDecimal RESALE_PRICE_LIMIT_RATE = BigDecimal.valueOf(1.20);

    // 리세일 수익 중 개최자 로열티 비율 (10%)
    public static final BigDecimal ORGANIZER_ROYALTY_RATE = BigDecimal.valueOf(0.10);

    // 리세일 온체인 등록 대기 시간(분 단위)
    public static final int RESALE_LISTING_TIMEOUT = 10;

}
