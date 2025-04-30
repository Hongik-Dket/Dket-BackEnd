package com.example.demo.domain.main.service.impl;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.domain.main.dto.EventCardListDTO;
import com.example.demo.domain.main.dto.OrganizerHomeResponseDTO;
import com.example.demo.domain.main.service.OrganizerHomeService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.domain.main.converter.MainConverter.toEventCardListDTO;
import static com.example.demo.domain.main.converter.MainConverter.toOrganizerHomeResponseDTO;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrganizerHomeServiceImpl implements OrganizerHomeService {

    private final UserService userService;
    private final EventRepository eventRepository;

    @Override
    public OrganizerHomeResponseDTO getHomeForOrganizer() {
        User user = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").ascending());

        Page<Event> todayEvents = eventRepository.findByOrganizerAndEventStatus(user, EventStatus.IN_PROGRESS, pageable);

        Page<Event> recentlyClosedApply = eventRepository.findByOrganizerAndApplyEndBetween(
                user,
                LocalDateTime.now().minusHours(24).withSecond(0).withNano(0),
                LocalDateTime.now().withSecond(0).withNano(0),
                PageRequest.of(0, 10, Sort.by("applyEnd").descending())
        );

        Page<Event> allEvents = eventRepository.findByOrganizerAndEventStatusNot(user, EventStatus.ENDED, pageable);

        Page<Event> endedEvents = Page.empty();
        if (allEvents.getTotalElements() < 10) {
            endedEvents = eventRepository.findByOrganizerAndEventStatus(
                    user, EventStatus.ENDED, PageRequest.of(
                            0,
                            10 - (int) allEvents.getTotalElements(),
                            Sort.by("endDate").descending()
                    )
            );
        }

        return toOrganizerHomeResponseDTO(todayEvents, recentlyClosedApply, allEvents, endedEvents);
    }

    @Override
    public EventCardListDTO getTodayEventsForOrganizer() {
        User user = userService.getCurrentUser();

        Page<Event> events = eventRepository.findByOrganizerAndEventStatus(
                user, EventStatus.IN_PROGRESS,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("startDate").ascending())
        );

        return toEventCardListDTO(events.getContent());
    }

    @Override
    public EventCardListDTO getClosedEventsForOrganizer() {
        User user = userService.getCurrentUser();

        Page<Event> events = eventRepository.findByOrganizerAndApplyEndBetween(
                user,
                LocalDateTime.now().minusHours(24).withSecond(0).withNano(0),
                LocalDateTime.now().withSecond(0).withNano(0),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("applyEnd").descending())
        );

        return toEventCardListDTO(events.getContent());
    }

    @Override
    public EventCardListDTO getAllEventsForOrganizer() {
        User user = userService.getCurrentUser();

        Page<Event> activeEvents = eventRepository.findByOrganizerAndEventStatusNot(
                user, EventStatus.ENDED,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("startDate").ascending())
        );

        Page<Event> endedEvents = eventRepository.findByOrganizerAndEventStatus(
                user, EventStatus.ENDED,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("endDate").descending())
        );

        List<Event> allEvents = new ArrayList<>();
        allEvents.addAll(activeEvents.getContent());
        allEvents.addAll(endedEvents.getContent());

        return toEventCardListDTO(allEvents);
    }

}
