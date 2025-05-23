package com.example.demo.global.infra.blockchain.converter;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.global.infra.blockchain.dto.CreateEventRequestDTO;

import java.util.List;

public class BlockchainConverter {

    public static CreateEventRequestDTO toCreateEventRequestDTO (Event event, List<String> photoCardURIs) {
        return CreateEventRequestDTO.builder()
                .eventId(event.getId())
                .organizerAddress(event.getOrganizer().getWalletAddress())
                .title(event.getTitle())
                .maxWinners(event.getCapacity())
                .priceKrw(event.getPriceKrw())
                .photoCardURIs(photoCardURIs)
                .build();
    }
}
