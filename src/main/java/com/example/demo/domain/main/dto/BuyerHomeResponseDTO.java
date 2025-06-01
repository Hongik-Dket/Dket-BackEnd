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

    private List<EventCardDTO> popularEvents;

    private List<EventCardDTO> appliedEvents;

    private List<EventCardDTO> purchasedEvents;

}
