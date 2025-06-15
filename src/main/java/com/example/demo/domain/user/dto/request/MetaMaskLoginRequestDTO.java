package com.example.demo.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MetaMaskLoginRequestDTO {
    private String walletAddress;
}