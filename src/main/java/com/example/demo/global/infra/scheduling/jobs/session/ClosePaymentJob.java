package com.example.demo.global.infra.scheduling.jobs.session;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.global.infra.blockchain.service.DketResaleService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClosePaymentJob implements Job {

    private final SessionRepository sessionRepository;
    private final ResaleRepository resaleRepository;
    private final DketResaleService dketResaleService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long sessionId = context.getJobDetail().getJobDataMap().getLong("sessionId");

        log.info("closePaymentJob: session [{}]", sessionId);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        session.setIsBuyable(false);

        List<Long> resaleIds = resaleRepository.findIdsBySessionIdAndStatus(
                sessionId, EnumSet.of(ResaleStatus.LISTING, ResaleStatus.AVAILABLE, ResaleStatus.RESERVED));
        resaleRepository.updateBySessionIdAndResaleStatusIn(
                sessionId,
                EnumSet.of(ResaleStatus.LISTING, ResaleStatus.AVAILABLE, ResaleStatus.RESERVED),
                ResaleStatus.CANCELED
        );

        dketResaleService.cancelResaleBatch(resaleIds);
    }
}
