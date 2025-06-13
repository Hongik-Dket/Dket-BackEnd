package com.example.demo.domain.apply.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyResponseDTO {
    private Long applyId;
    private Long sessionId;
    private LocalDateTime appliedAt;
}