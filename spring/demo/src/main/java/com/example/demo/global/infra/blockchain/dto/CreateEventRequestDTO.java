package com.example.demo.global.infra.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequestDTO {
    Long eventId;
    String organizerAddress;
    String title;
    int maxWinners;
    int priceKrw;
    List<String> photoCardURIs;
}
