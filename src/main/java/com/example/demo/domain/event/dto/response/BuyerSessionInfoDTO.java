package com.example.demo.domain.event.dto.response;

import com.example.demo.domain.apply.enums.ApplyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerSessionInfoDTO {
    private Long sessionId;
    private LocalDate date;

    private int paidCount;

    private ApplyStatus applyStatus; // 응모 상태: APPLIED, SELECTED, NOT_SELECTED, PAID, CANCELED, null
    private Long ticketId;           // 존재하면 값, 없으면 null

}
