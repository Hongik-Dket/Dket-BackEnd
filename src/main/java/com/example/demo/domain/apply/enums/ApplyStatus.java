package com.example.demo.domain.apply.enums;

public enum ApplyStatus {
    APPLIED,        // 응모 완료
    SELECTED,       // 당첨
    NOT_SELECTED,   // 탈락
    AWAITING_APPROVAL,  // 결제 승인 대기
    AWAITING_PAYMENT,     // 결제 승인됨, 아직 결제 전
    PAID,           // 결제 완료
    CANCELED,        // 당첨 취소
}
