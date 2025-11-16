package com.example.demo.domain.resale.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureDTO {

    Long resaleId;
    String challengeId;
    String signature;
    String publicKey;

}
