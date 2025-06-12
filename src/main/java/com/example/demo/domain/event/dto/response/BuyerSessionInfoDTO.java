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
    private ApplyStatus applyStatus;
    private Long ticketId;           // 존재하면 값, 없으면 null
    private boolean buyable;

}
