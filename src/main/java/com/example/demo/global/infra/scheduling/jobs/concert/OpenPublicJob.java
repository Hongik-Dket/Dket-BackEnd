package com.example.demo.global.infra.scheduling.jobs.concert;

import com.example.demo.domain.apply.service.ApplyService;
import com.example.demo.domain.concert.entity.Concert;
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

@Component
@RequiredArgsConstructor
public class OpenPublicJob implements Job {

    private final ConcertRepository concertRepository;
    private final ApplyService applyService;
    private final DketNFTService dketNFTService;
    private final SchedulingService schedulingService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long concertId = context.getJobDetail().getJobDataMap().getLong("concertId");

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(()->new CustomException(ErrorStatus.CONCERT_NOT_FOUND));

        concert.setConcertStatus(ConcertStatus.TICKETED);

        dketNFTService.openPublicSaleOnChain(concert);
        applyService.cancelWinnerTickets(concert);

        schedulingService.scheduleConcertJob(concert, StartConcertJob.class);
    }
}
