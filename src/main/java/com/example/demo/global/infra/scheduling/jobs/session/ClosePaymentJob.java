package com.example.demo.global.infra.scheduling.jobs.session;

import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ClosePaymentJob implements Job {

    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long sessionId = context.getJobDetail().getJobDataMap().getLong("sessionId");

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        session.setIsBuyable(false);
    }
}
