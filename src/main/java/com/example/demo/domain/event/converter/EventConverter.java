package com.example.demo.domain.event.converter;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.event.dto.request.EventUploadDTO;
import com.example.demo.domain.event.dto.response.BuyerEventInfoDTO;
import com.example.demo.domain.event.dto.response.BuyerSessionInfoDTO;
import com.example.demo.domain.event.dto.response.OrganizerEventInfoDTO;
import com.example.demo.domain.event.dto.response.OrganizerSessionInfoDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.metadata.dto.PhotoCardInfoDTO;
import com.example.demo.domain.user.entity.User;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class EventConverter {

    public static OrganizerEventInfoDTO toEventInfoDTO(Event event, List<PhotoCardInfoDTO> photoCardInfoDTOList) {
        return OrganizerEventInfoDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .posterUrl(event.getPosterUrl())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .ageLimit(event.getAgeLimit())
                .priceKrw(event.getPriceKrw())
                .applyStart(event.getApplyStart())
                .applyEnd(event.getApplyEnd())
                .capacity(event.getCapacity())
                .eventStatus(event.getEventStatus())
                .sessionIds(
                        event.getSessions().stream()
                                .map(Session::getId)
                                .collect(Collectors.toList())
                )
                .description(event.getDescription())
                .photoCardList(photoCardInfoDTOList)
                .build();
    }

    public static OrganizerSessionInfoDTO toSessionInfoDTO(Session session, int attendeeCount) {
        return OrganizerSessionInfoDTO.builder()
                .eventId(session.getEvent().getId())
                .sessionId(session.getId())
                .date(session.getDate())
                .applyCount(session.getApplyList().size())
                .paidCount(session.getTicketList().size())
                .attendeeCount(attendeeCount)
                .build();
    }

    public static Event toEvent(EventUploadDTO eventUploadDTO, User user, String bannerUrl, String posterUrl, BigInteger priceWei) {
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
                .priceKrw(eventUploadDTO.getPriceKrw())
                .capacity(eventUploadDTO.getCapacity())
                .applyStart(eventUploadDTO.getApplyStart())
                .applyEnd(eventUploadDTO.getApplyEnd())
                .bannerUrl(bannerUrl)
                .posterUrl(posterUrl)
                .priceWei(priceWei)
                .build();
    }

    public static BuyerEventInfoDTO toBuyerEventInfoDTO(Event event, List<BuyerSessionInfoDTO> sessionList) {
        return BuyerEventInfoDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .ageLimit(event.getAgeLimit())
                .priceKrw(event.getPriceKrw())
                .applyStart(event.getApplyStart())
                .applyEnd(event.getApplyEnd())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .posterUrl(event.getPosterUrl())
                .capacity(event.getCapacity())
                .eventStatus(event.getEventStatus())
                .sessionList(sessionList)
                .build();
    }

    public static BuyerSessionInfoDTO toBuyerSessionInfoDTO(Session session, ApplyStatus applyStatus, Long ticketId, boolean buyable) {
        return BuyerSessionInfoDTO.builder()
                .sessionId(session.getId())
                .date(session.getDate())
                .paidCount(session.getTicketList().size())
                .applyStatus(applyStatus)
                .ticketId(ticketId)
                .buyable(buyable)
                .build();
    }
}
