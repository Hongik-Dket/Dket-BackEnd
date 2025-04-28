package com.example.demo.domain.event.converter;

import com.example.demo.domain.event.dto.EventInfoDTO;
import com.example.demo.domain.event.dto.SessionInfoDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;

import java.util.Map;
import java.util.stream.Collectors;

public class EventConverter {

    public static EventInfoDTO toEventInfoDTO(Event event) {
        return EventInfoDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .posterUrl(event.getPosterUrl())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .ageLimit(event.getAgeLimit())
                .price(event.getPrice())
                .applyStart(event.getApplyStart())
                .applyEnd(event.getApplyEnd())
                .capacity(event.getCapacity())
                .eventStatus(event.getEventStatus())
                .sessionIds(
                        event.getSessions().stream()
                                .map(Session::getId)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public static SessionInfoDTO toSessionInfoDTO(Session session, int attendeeCount) {
        return SessionInfoDTO.builder()
                .eventId(session.getEvent().getId())
                .sessionId(session.getId())
                .date(session.getDate())
                .applyCount(session.getApplyList().size())
                .paidCount(session.getTicketList().size())
                .attendeeCount(attendeeCount)
                .build();
    }
}
