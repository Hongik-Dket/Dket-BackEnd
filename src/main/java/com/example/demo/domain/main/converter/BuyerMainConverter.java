package com.example.demo.domain.main.converter;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.EventCardDTO;
import com.example.demo.domain.main.dto.EventCardListDTO;

import java.util.List;
import java.util.stream.Collectors;

public class BuyerMainConverter {

    private static EventCardDTO toEventCardDTO(Event event) {
        return EventCardDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .eventStatus(event.getEventStatus())
                .imageUrl(event.getPosterUrl())
                .build();
    }

    private static List<EventCardDTO> toEventCardDTOList(List<Event> events) {
        return events.stream()
                .map(BuyerMainConverter::toEventCardDTO)
                .collect(Collectors.toList());
    }

    public static BuyerHomeResponseDTO toBuyerHomeResponseDTO(
            List<Event> popularEvents,
            List<Event> appliedEvents,
            List<Event> purchasedEvents
    ) {
        return BuyerHomeResponseDTO.builder()
                .popularEvents(toEventCardDTOList(popularEvents))
                .appliedEvents(toEventCardDTOList(appliedEvents))
                .purchasedEvents(toEventCardDTOList(purchasedEvents))
                .build();
    }

    public static EventCardListDTO toEventCardListDTO(List<Event> events) {
        return EventCardListDTO.builder()
                .eventCardList(toEventCardDTOList(events))
                .build();
    }
}
