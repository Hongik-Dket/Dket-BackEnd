package com.example.demo.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetaMaskDTO {
    private boolean isSuccess;
    private String code;
    private String message;
}