package com.example.demo.domain.main.converter;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.EventCardDTO;
import com.example.demo.domain.main.dto.EventCardListDTO;

import java.util.List;
import java.util.stream.Collectors;

public class BuyerMainConverter {

    private static EventCardDTO toEventCardDTO(Event event, boolean usePoster) {
        String imageUrl = usePoster ? event.getPosterUrl() : event.getBannerUrl();

        return EventCardDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .eventStatus(event.getEventStatus())
                .imageUrl(imageUrl)
                .build();
    }

    private static List<EventCardDTO> toEventCardDTOList(List<Event> events, boolean usePoster) {
        return events.stream()
                .map(event -> toEventCardDTO(event, usePoster))
                .collect(Collectors.toList());
    }

    public static BuyerHomeResponseDTO toBuyerHomeResponseDTO(
            List<Event> popularEvents,
            List<Event> appliedEvents,
            List<Event> purchasedEvents,
            List<Event> entireEvents
    ) {
        return new BuyerHomeResponseDTO(
                toEventCardDTOList(popularEvents, true),
                toEventCardDTOList(appliedEvents, true),
                toEventCardDTOList(purchasedEvents, true),
                toEventCardDTOList(entireEvents, true)
        );
    }

    public static EventCardListDTO toEventCardListDTO(List<Event> events) {
        return new EventCardListDTO(toEventCardDTOList(events, false));
    }
}
