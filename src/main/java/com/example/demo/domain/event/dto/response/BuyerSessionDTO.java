package com.example.demo.domain.event.dto.response;

import com.example.demo.domain.apply.enums.ApplyStatus;

import java.time.LocalDate;

public class BuyerSessionDTO {
    private Long sessionId;
    private LocalDate date;

    private int paidCount;

    private ApplyStatus applyStatus; // 응모 상태: APPLIED, SELECTED, NOT_SELECTED, PAID, CANCELED, null
    private Long ticketId;           // 존재하면 값, 없으면 null
}
