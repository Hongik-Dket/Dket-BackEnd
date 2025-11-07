package com.example.demo.global.infra.scheduling.jobs.concert;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.concert.repository.ConcertRepository;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CloseApplyJob implements Job {

    private final ConcertRepository concertRepository;
    private final SchedulingService schedulingService;
    private final DketNFTService dketNFTService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long concertId = context.getJobDetail().getJobDataMap().getLong("concertId");

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(()->new CustomException(ErrorStatus.CONCERT_NOT_FOUND));

        concert.setConcertStatus(ConcertStatus.APPLY_CLOSED);

        int emptyCount = 0;
        List<Session> sessions = concert.getSessions();

        for (Session session : sessions) {
            if (session.getApplyList().isEmpty()) {
                emptyCount++;
                dketNFTService.setDrawnOnChain(session);
            } else {
                // TODO: setApplicantsListCommitment
            }
        }

        if (emptyCount == sessions.size()) {
            dketNFTService.openPublicSaleOnChain(concert);
            concert.setConcertStatus(ConcertStatus.TICKETED);
            schedulingService.scheduleConcertJob(concert, OpenPublicJob.class);
        }
    }
}
