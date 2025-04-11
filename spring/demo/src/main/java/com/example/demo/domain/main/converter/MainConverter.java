package com.example.demo.domain.main.converter;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.main.dto.EventCardDTO;
import com.example.demo.domain.main.dto.OrganizerHomeResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class MainConverter {

    private static EventCardDTO toEventCardDTO(Event event) {
        return EventCardDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .eventStatus(event.getEventStatus())
                .build();
    }

    private static List<EventCardDTO> toEventCardDTOList(List<Event> events) {
        return events.stream()
                .map(MainConverter::toEventCardDTO)
                .toList();
    }

    public static OrganizerHomeResponseDTO toOrganizerHomeResponseDTO(
            List<Event> todayEvents,
            List<Event> recentlyClosedApplyEvents,
            List<Event> otherEvents
    ) {
        return OrganizerHomeResponseDTO.builder()
                .todayEvents(toEventCardDTOList(todayEvents))
                .todayEventCount(todayEvents.size())
                .recentlyClosedApplyEvents(toEventCardDTOList(recentlyClosedApplyEvents))
                .recentlyClosedApplyEventCount(recentlyClosedApplyEvents.size())
                .otherEvents(toEventCardDTOList(otherEvents))
                .otherEventCount(otherEvents.size())
                .build();
    }

}
