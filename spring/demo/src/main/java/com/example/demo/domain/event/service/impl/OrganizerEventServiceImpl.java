package com.example.demo.domain.event.service.impl;

import com.example.demo.domain.event.dto.SessionInfoDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.domain.event.dto.EventInfoDTO;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.event.service.OrganizerEventService;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.example.demo.domain.event.converter.EventConverter.toEventInfoDTO;
import static com.example.demo.domain.event.converter.EventConverter.toSessionInfoDTO;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrganizerEventServiceImpl implements OrganizerEventService {

    private final UserService userService;
    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;

    @Override
    public EventInfoDTO getEventInfoForOrganizer(Long eventId) {
        User user = userService.getCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        if (!event.getOrganizer().equals(user))
            throw new CustomException(ErrorStatus.EVENT_ORGANIZER_MISMATCH);

        return toEventInfoDTO(event);

    }

    @Override
    public SessionInfoDTO getSessionInfoForOrganizer(Long eventId, Long sessionId) {
        User user = userService.getCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        if (!event.getOrganizer().equals(user))
            throw new CustomException(ErrorStatus.EVENT_ORGANIZER_MISMATCH);

        if (!session.getEvent().equals(event))
            throw new CustomException(ErrorStatus.EVENT_SESSION_MISMATCH);

        int attendeeCount = (int) session.getTicketList().stream()
                .filter(ticket -> ticket.getEnteredAt() != null)
                .count();

        return toSessionInfoDTO(session, attendeeCount);
    }
}
