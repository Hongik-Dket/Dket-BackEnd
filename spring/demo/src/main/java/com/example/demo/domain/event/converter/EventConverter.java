package com.example.demo.domain.event.converter;

import com.example.demo.domain.event.dto.request.EventUploadDTO;
import com.example.demo.domain.event.dto.response.EventInfoDTO;
import com.example.demo.domain.event.dto.response.SessionInfoDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.user.entity.User;

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

    public static Event toEvent(EventUploadDTO eventUploadDTO, User user, String bannerUrl, String posterUrl) {
        return Event.builder()
                .organizer(user)
                .title(eventUploadDTO.getTitle())
                .ageLimit(eventUploadDTO.getAgeLimit())
                .location(eventUploadDTO.getLocation())
                .description(eventUploadDTO.getDescription())
                .startDate(eventUploadDTO.getStartDate())
                .endDate(eventUploadDTO.getEndDate())
                .startTime(eventUploadDTO.getStartTime())
                .endTime(eventUploadDTO.getEndTime())
                .price(eventUploadDTO.getPrice())
                .capacity(eventUploadDTO.getCapacity())
                .applyStart(eventUploadDTO.getApplyStart())
                .applyEnd(eventUploadDTO.getApplyEnd())
                .bannerUrl(bannerUrl)
                .posterUrl(posterUrl)
                .build();
    }
}
