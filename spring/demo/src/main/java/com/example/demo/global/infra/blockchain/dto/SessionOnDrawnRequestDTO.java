package com.example.demo.global.infra.blockchain.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SessionOnDrawnRequestDTO {
    String sessionId;
    List<String> winners;
}
