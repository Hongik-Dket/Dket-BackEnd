package com.example.demo.domain.apply.service;

import com.example.demo.domain.apply.DTO.ApplyResponseDTO;
import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private final ApplyRepository applyRepository;
    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void cancelWinnerTickets(Event event) {

        List<Long> sessionIds = eventRepository.findSessionIdsByEventId(event.getId());

        for (Long sessionId : sessionIds)
            applyRepository.batchUpdateApplyStatusBySessionId(sessionId, ApplyStatus.SELECTED, ApplyStatus.CANCELED);

    }

    @Override
    @Transactional
    public ApplyResponseDTO applyToSession(Long eventId, Long sessionId) {
        User user = userService.getCurrentUser();

        // 1. Event 존재 확인
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        // 2. Session 존재, Event 일치 여부 확인
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));
        if (!session.getEvent().getId().equals(eventId)) {
            throw new CustomException(ErrorStatus.EVENT_SESSION_MISMATCH);
        }

        // 3. 공연 상태 및 응모 기간 확인
        if (!event.getEventStatus().equals(EventStatus.APPLY_OPEN)) {
            throw new CustomException(ErrorStatus.EVENT_NOT_OPEN_FOR_APPLY);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getApplyStart()) || now.isAfter(event.getApplyEnd())) {
            throw new CustomException(ErrorStatus.APPLY_TIME_INVALID);
        }

        // 4. 중복 응모 방지
        boolean alreadyApplied = applyRepository.existsByUserIdAndSessionId(user.getId(), sessionId);
        if (alreadyApplied) {
            throw new CustomException(ErrorStatus.ALREADY_APPLIED);
        }

        if (!user.isEligibleFor(session.getEvent().getAgeLimit()))
            throw new CustomException(ErrorStatus.APPLY_AGE_RESTRICTED);

        // 5. 저장
        Apply apply = Apply.builder()
                .session(session)
                .user(user)
                .applyStatus(ApplyStatus.APPLIED)
                .build();

        Apply saved = applyRepository.saveAndFlush(apply);

        user.addApply(saved);
        session.addApply(saved);

        return ApplyResponseDTO.builder()
                .applyId(saved.getId())
                .sessionId(sessionId)
                .appliedAt(saved.getCreatedAt())
                .build();
    }
}
