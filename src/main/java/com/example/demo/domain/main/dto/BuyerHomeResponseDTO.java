package com.example.demo.domain.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerHomeResponseDTO {

    private List<ConcertCardDTO> popularConcerts;

    private List<ConcertCardDTO> appliedConcerts;

    private List<ConcertCardDTO> purchasedConcerts;

    private List<ConcertCardDTO> entireConcerts;

}
