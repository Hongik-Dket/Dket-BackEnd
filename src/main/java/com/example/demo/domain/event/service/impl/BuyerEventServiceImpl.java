package com.example.demo.domain.event.service.impl;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.converter.EventConverter;
import com.example.demo.domain.event.dto.response.BuyerEventInfoDTO;
import com.example.demo.domain.event.dto.response.BuyerSessionInfoDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
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
        User buyer = userService.getCurrentUser();  // 현재 로그인한 구매자

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        List<BuyerSessionInfoDTO> sessionDTOList = new ArrayList<>();

        for (Session session : event.getSessions()) {
            // Apply 조회
            Optional<Apply> apply = applyRepository.findBySessionAndUser(session, buyer);

            ApplyStatus applyStatus = null;
            Long ticketId = null;

            if (apply.isPresent()) {
                applyStatus = apply.get().getApplyStatus();

                // 티켓이 있는 경우
                Optional<Ticket> ticket = ticketRepository.findByUserAndSession(buyer, session);
                if (ticket.isPresent()) {
                    ticketId = ticket.get().getId();
                }
            }

            BuyerSessionInfoDTO sessionDTO = EventConverter.toBuyerSessionInfoDTO(
                    session,
                    applyStatus,
                    ticketId
            );

            sessionDTOList.add(sessionDTO);
        }

        return EventConverter.toBuyerEventInfoDTO(event, sessionDTOList);
    }
}
