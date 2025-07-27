package com.example.demo.domain.metadata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoCardDetailDTO {
    private Long photoCardId;
    private Long ticketId;
    private String imageUrl;
    private String concertTitle;
    private LocalDate sessionDate;
    private String ticketNumber;
    private String nftUrl;
}
