package com.example.demo.domain.event.converter;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.event.dto.response.BuyerSessionInfoDTO;
import com.example.demo.domain.event.dto.response.OrganizerSessionInfoDTO;
import com.example.demo.domain.event.entity.Session;

public class SessionConverter {
    public static OrganizerSessionInfoDTO toOrganizerSessionInfoDTO(
            Session session, int attendeeCount) {

        return OrganizerSessionInfoDTO.builder()
                .eventId(session.getEvent().getId())
                .sessionId(session.getId())
                .date(session.getDate())
                .applyCount(session.getApplyList().size())
                .paidCount(session.getPaidCount())
                .attendeeCount(attendeeCount)
                .build();
    }

    public static BuyerSessionInfoDTO toBuyerSessionInfoDTO(Session session, ApplyStatus applyStatus, Long ticketId, boolean buyable) {
        return BuyerSessionInfoDTO.builder()
                .sessionId(session.getId())
                .date(session.getDate())
                .paidCount(session.getPaidCount())
                .applyStatus(applyStatus)
                .ticketId(ticketId)
                .buyable(buyable)
                .build();
    }
}
