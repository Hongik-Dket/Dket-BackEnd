package com.example.demo.global.infra.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequestDTO {
    Long eventId;
    Long sessionId;
    List<String> applications;
}
