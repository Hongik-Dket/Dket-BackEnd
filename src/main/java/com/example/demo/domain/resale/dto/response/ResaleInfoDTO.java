package com.example.demo.domain.resale.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResaleInfoDTO {

    private Long resaleId;
    private Long tokenId;
}
