package com.example.demo.global.infra.scheduling.jobs.concert;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.concert.repository.ConcertRepository;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EndConcertJob implements Job {

    private final ConcertRepository concertRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long concertId = context.getJobDetail().getJobDataMap().getLong("concertId");

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(()->new CustomException(ErrorStatus.CONCERT_NOT_FOUND));

        concert.setConcertStatus(ConcertStatus.ENDED);
    }
}
