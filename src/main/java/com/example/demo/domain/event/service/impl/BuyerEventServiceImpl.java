package com.example.demo.domain.event.service.impl;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.converter.EventConverter;
import com.example.demo.domain.event.dto.response.BuyerEventDetailDTO;
import com.example.demo.domain.event.dto.response.BuyerSessionInfoDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.repository.EventRepository;
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.demo.domain.event.converter.SessionConverter.toBuyerSessionInfoDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerEventServiceImpl implements BuyerEventService {

    private final EventRepository eventRepository;
    private final ApplyRepository applyRepository;
    private final TicketRepository ticketRepository;
    private final UserService userService;

    @Override
    public BuyerEventDetailDTO getEventDetailForBuyer(Long eventId) {
        User user = userService.getCurrentUser();  // 현재 로그인한 구매자

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        List<Session> sessions = event.getSessions();

        List<Long> sessionIds = sessions.stream()
                .map(Session::getId)
                .toList();

        List<Apply> applyList = applyRepository.findByUserIdAndSessionIdIn(user.getId(), sessionIds);
        Map<Long, Apply> applyMap = applyList.stream()
                .collect(Collectors.toMap(apply -> apply.getSession().getId(), Function.identity()));

        List<Ticket> ticketList = ticketRepository.findByUserIdAndSessionIdIn(user.getId(), sessionIds);
        Map<Long, Ticket> ticketMap = ticketList.stream()
                .collect(Collectors.toMap(ticket -> ticket.getSession().getId(), Function.identity()));

        List<BuyerSessionInfoDTO> sessionDTOs = sessions.stream().map(session -> {
            Apply apply = applyMap.get(session.getId());
            ApplyStatus applyStatus = apply != null ? apply.getApplyStatus() : null;

            Ticket ticket = ticketMap.get(session.getId());
            Long ticketId = ticket != null ? ticket.getId() : null;

            boolean buyable = validateBuyer(session, user, apply);

            return toBuyerSessionInfoDTO(session, applyStatus, ticketId, buyable);
        }).toList();

        return EventConverter.toBuyerEventInfoDTO(event, sessionDTOs);
    }

    private boolean validateBuyer(Session session, User user, Apply apply) {
        Event event = session.getEvent();

        if (!user.isEligibleFor(event.getAgeLimit()) || !session.getIsBuyable()) {
            return false;
        }

        if (ticketRepository.existsByUserIdAndSessionId(user.getId(), session.getId())) {
            return false;
        }

        switch (event.getEventStatus()) {
            case APPLY_CLOSED:
                return apply != null && apply.getApplyStatus() == ApplyStatus.SELECTED;

            case TICKETED:
            case IN_PROGRESS:
                return true;

            default:
                return false;
        }
    }
}
