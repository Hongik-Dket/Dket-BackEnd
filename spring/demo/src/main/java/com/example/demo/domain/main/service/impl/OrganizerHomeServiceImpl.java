package com.example.demo.domain.main.service.impl;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.domain.main.dto.OrganizerHomeResponseDTO;
import com.example.demo.domain.main.service.OrganizerHomeService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.demo.domain.main.converter.MainConverter.toOrganizerHomeResponseDTO;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrganizerHomeServiceImpl implements OrganizerHomeService {

    private final UserService userService;
    private final EventRepository eventRepository;

    @Override
    public OrganizerHomeResponseDTO getOrganizerHome() {
        User user = userService.getCurrentUser();

        // 1. 오늘 공연 (IN_PROGRESS)
        List<Event> todayEvents = eventRepository.findByOrganizerAndEventStatus(user, EventStatus.IN_PROGRESS);

        // 2. 최근 24시간 이내 응모 마감된 공연
        List<Event> recentlyClosedApply = eventRepository.findByOrganizerAndApplyEndBetweenOrderByApplyEndDesc(
                user,
                LocalDateTime.now().minusHours(24).withSecond(0).withNano(0),
                LocalDateTime.now().withSecond(0).withNano(0)
                );

        // 3. 그 외 공연 (종료 제외, 시작일 기준 오름차순 정렬)
        List<Event> otherEvents = eventRepository.findByOrganizerAndEventStatusNotOrderByStartDateAsc(user, EventStatus.ENDED);
        Set<Long> excludedIds = Stream.concat(todayEvents.stream(), recentlyClosedApply.stream())
                .map(Event::getId)
                .collect(Collectors.toSet());

        List<Event> filteredOthers = otherEvents.stream()
                .filter(event -> !excludedIds.contains(event.getId()))
                .toList();

        return toOrganizerHomeResponseDTO(todayEvents, recentlyClosedApply, filteredOthers);
    }
}
