package com.example.demo.domain.resale.enums;

public enum ResaleStatus {
    LISTING,    // 온체인 등록 진행 중
    AVAILABLE,  // 구매 가능
    RESERVED,   // 예약 상태(구매 불가)
    PENDING,    // 구매 대기
    SOLD,       // 판매 완료
    CANCELED    // 취소(온체인 등록 실패 시)
}
