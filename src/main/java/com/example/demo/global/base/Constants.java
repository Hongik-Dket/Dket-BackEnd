package com.example.demo.global.base;

public class Constants {

    // 응모 마감 이후 결제 가능 기한(일 단위): 마감일 제외 2일 후 자정
    public static final int PAYMENT_DEADLINE = 3;

    // 공연 시작 전 결제 가능 기한(시간 단위): 공연 시작 2시간 전까지 결제 가능
    public static final int PAYMENT_AVAILABLE_BEFORE_CONCERT_START = 2;

    // OpenSea base url
    public static final String OPENSEA_BASE_URL = "https://testnets.opensea.io/assets/sepolia/";

    // 리세일 티켓 예약 만료 시간(분 단위)
    public static final int RESALE_RESERVATION_EXPIRATION_MINUTES = 5;

}
