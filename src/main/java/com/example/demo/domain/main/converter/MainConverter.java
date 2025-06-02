package com.example.demo.domain.main.converter;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.EventCardDTO;
import com.example.demo.domain.main.dto.EventCardListDTO;
import com.example.demo.domain.main.dto.OrganizerHomeResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public class MainConverter {

    private static EventCardDTO toEventCardDTO(Event event, boolean poster) {
        String image;

        if (poster)
            image = event.getPosterUrl();
        else
            image = event.getBannerUrl();

        return EventCardDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .eventStatus(event.getEventStatus())
                .imageUrl(image)
                .build();
    }

    private static List<EventCardDTO> toEventCardDTOList(List<Event> events, boolean poster) {
        return events.stream()
                .map((Event event) -> toEventCardDTO(event, poster))
                .toList();
    }

    private static List<EventCardDTO> toEventCardDTOListFromPage(Page<Event> events, boolean poster) {
        return toEventCardDTOList(events.getContent(), poster);
    }

    public static OrganizerHomeResponseDTO toOrganizerHomeResponseDTO(
            Page<Event> todayEvents,
            Page<Event> recentlyClosedApplyEvents,
            Page<Event> otherEvents,
            Page<Event> endedEvents
    ) {
        return OrganizerHomeResponseDTO.builder()
                .todayEvents(toEventCardDTOListFromPage(todayEvents, true))
                .recentlyClosedApplyEvents(toEventCardDTOListFromPage(recentlyClosedApplyEvents, true))
                .allEvents(toEventCardDTOListFromPage(otherEvents, true))
                .endedEvents(toEventCardDTOListFromPage(endedEvents, true))
                .build();
    }

    public static EventCardListDTO toEventCardListDTO(List<Event> events) {
        return EventCardListDTO.builder()
                .eventCardList(toEventCardDTOList(events, false))
                .build();
    }

    // 구매자 홈 기능
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
}
