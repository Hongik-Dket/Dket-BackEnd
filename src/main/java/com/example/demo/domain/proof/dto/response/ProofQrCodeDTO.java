package com.example.demo.domain.proof.dto.response;

import com.example.demo.domain.user.enums.IdentityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofQrCodeDTO {

    private String qrCodeUrl;
    private IdentityType identityType;

}
