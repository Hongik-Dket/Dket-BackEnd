package com.example.demo.domain.event.service.impl;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.converter.EventConverter;
import com.example.demo.domain.event.dto.response.BuyerEventInfoDTO;
import com.example.demo.domain.event.dto.response.BuyerSessionInfoDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.event.service.BuyerEventService;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerEventServiceImpl implements BuyerEventService {

    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;
    private final TicketRepository ticketRepository;
    private final UserService userService;

    @Override
    public BuyerEventInfoDTO getEventDetailForBuyer(Long eventId) {
        User user = userService.getCurrentUser();  // 현재 로그인한 구매자

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        List<Session> sessions = event.getSessions();

        List<BuyerSessionInfoDTO> sessionDTOs = sessions.stream().map(session -> {
            // 해당 세션에 대해 유저의 응모 정보 가져오기
            Apply apply = applyRepository.findBySessionAndUser(session, user).orElse(null);
            ApplyStatus applyStatus = apply != null ? apply.getApplyStatus() : null;

            // 해당 세션에 대해 유저의 티켓 정보 가져오기
            Ticket ticket = ticketRepository.findByUserAndSession(user, session).orElse(null);
            Long ticketId = ticket != null ? ticket.getId() : null;

            // 잔여 티켓 계산
            int paidCount = session.getTicketList().size();
            boolean buyable = event.getEventStatus() == EventStatus.TICKETED
                    && (event.getCapacity() - paidCount > 0)
                    && applyStatus != ApplyStatus.PAID;

            return BuyerSessionInfoDTO.builder()
                    .sessionId(session.getId())
                    .date(session.getDate())
                    .paidCount(paidCount)
                    .applyStatus(applyStatus)
                    .ticketId(ticketId)
                    .buyable(buyable)
                    .build();
        }).toList();

        return EventConverter.toBuyerEventInfoDTO(event, sessionDTOs);
    }
}
