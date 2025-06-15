package com.example.demo.domain.metadata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoCardDTO {

    private Long photoCardId;
    private String ipfsUrl;
}
