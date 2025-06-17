package com.example.demo.global.security.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MetaMaskLoginRequestDTO {
    private String walletAddress;
}