package com.example.demo.domain.proof.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WinProofAuthDTO {

    Long sessionId;
    String challengeId;
    String signature;
    String publicKey;

}
