package com.example.demo.domain.user.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class MetaMaskDTO {
    private boolean isSuccess;
    private String code;
    private String message;
}