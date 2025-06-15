package com.example.demo.domain.event.service.impl;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.event.service.SessionService;
import com.example.demo.global.event.ReadyToMintEvent;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.infra.scheduling.jobs.event.OpenPublicJob;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;
    private final SchedulingService schedulingService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void saveWinners(Long sessionId, List<String> winners) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        if (session.getIsDrawn())
            throw new CustomException(ErrorStatus.SESSION_ALREADY_DRAWN);

        applyRepository.batchUpdateApplyStatusBySessionIdAndWalletAddresses(
                session.getId(), winners, ApplyStatus.SELECTED);

        applyRepository.batchUpdateStatusExceptWallets(
                session.getId(), winners, ApplyStatus.NOT_SELECTED);

        Event event = session.getEvent();
        if (event.getEventStatus() != EventStatus.APPLY_CLOSED) {
            event.setEventStatus(EventStatus.APPLY_CLOSED);
            schedulingService.scheduleEventJob(event, OpenPublicJob.class);
        }
    }

    @Override
    @Transactional
    public void completeDraw(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        session.setIsDrawn();

        if(session.getMetadataUploaded())
            eventPublisher.publishEvent(new ReadyToMintEvent(session.getId()));
    }
}
