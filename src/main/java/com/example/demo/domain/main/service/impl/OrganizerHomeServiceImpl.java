package com.example.demo.domain.main.service.impl;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.concert.repository.ConcertRepository;
import com.example.demo.domain.main.dto.ConcertCardListDTO;
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

import static com.example.demo.domain.main.converter.MainConverter.toConcertCardListDTO;
import static com.example.demo.domain.main.converter.MainConverter.toOrganizerHomeResponseDTO;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrganizerHomeServiceImpl implements OrganizerHomeService {

    private final UserService userService;
    private final ConcertRepository concertRepository;

    @Override
    public OrganizerHomeResponseDTO getHomeForOrganizer() {
        User user = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").ascending());

        Page<Concert> todayConcerts = concertRepository.findByOrganizerIdAndConcertStatus(user.getId(), ConcertStatus.IN_PROGRESS, pageable);

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime oneDayAgo = now.minusHours(24);

        Page<Concert> recentlyClosedApply = concertRepository.findRecentlyClosedApply(
                user.getId(),
                oneDayAgo,
                now,
                PageRequest.of(0, 10, Sort.by("applyEnd").descending())
        );

        Page<Concert> allConcerts = concertRepository.findByOrganizerIdAndConcertStatusNot(
                user.getId(), ConcertStatus.ENDED, pageable);

        Page<Concert> endedConcerts = Page.empty();
        if (allConcerts.getTotalElements() < 10) {
            endedConcerts = concertRepository.findByOrganizerIdAndConcertStatus(
                    user.getId(),
                    ConcertStatus.ENDED,
                    PageRequest.of(
                            0,
                            10 - (int) allConcerts.getTotalElements(),
                            Sort.by("endDate").descending()
                    )
            );
        }

        return toOrganizerHomeResponseDTO(todayConcerts, recentlyClosedApply, allConcerts, endedConcerts);
    }

    @Override
    public ConcertCardListDTO getTodayConcertsForOrganizer() {
        User user = userService.getCurrentUser();

        Page<Concert> concerts = concertRepository.findByOrganizerIdAndConcertStatus(
                user.getId(),
                ConcertStatus.IN_PROGRESS,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("startDate").ascending())
        );

        return toConcertCardListDTO(concerts.getContent());
    }

    @Override
    public ConcertCardListDTO getClosedConcertsForOrganizer() {
        User user = userService.getCurrentUser();

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime oneDayAgo = now.minusHours(24);

        Page<Concert> concerts = concertRepository.findRecentlyClosedApply(
                user.getId(),
                oneDayAgo,
                now,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("applyEnd").descending())
        );

        return toConcertCardListDTO(concerts.getContent());
    }

    @Override
    public ConcertCardListDTO getAllConcertsForOrganizer() {
        User user = userService.getCurrentUser();

        Page<Concert> concerts = concertRepository.findSortedForOrganizer(
                user.getId(), Pageable.unpaged()
        );

        return toConcertCardListDTO(concerts.getContent());
    }

}
