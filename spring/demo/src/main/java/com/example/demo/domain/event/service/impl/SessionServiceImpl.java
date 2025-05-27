package com.example.demo.domain.event.service.impl;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.event.service.SessionService;
import com.example.demo.global.infra.blockchain.dto.SessionOnDrawnRequestDTO;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.infra.scheduling.jobs.event.OpenPublicJob;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;
    private final SchedulingService schedulingService;

    @Override
    @Transactional
    public void saveWinners(SessionOnDrawnRequestDTO request) {
        Session session = sessionRepository.findById(Long.valueOf(request.getSessionId()))
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        applyRepository.batchUpdateApplyStatusBySessionIdAndWalletAddresses(
                session.getId(), request.getWinners(), ApplyStatus.SELECTED);

        applyRepository.batchUpdateStatusExceptWallets(
                session.getId(), request.getWinners(), ApplyStatus.NOT_SELECTED);

        Event event = session.getEvent();
        if (event.getEventStatus() != EventStatus.APPLY_CLOSED) {
            event.setEventStatus(EventStatus.APPLY_CLOSED);
            schedulingService.scheduleEventJob(event, OpenPublicJob.class);
        }
    }
}
