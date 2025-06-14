package com.example.demo.domain.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDTO {

    Long sessionId;
    BigInteger priceWei;

}
