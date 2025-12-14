package com.example.demo.global.infra.scheduling.jobs.resale;

import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.global.infra.blockchain.service.DketResaleService;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.demo.global.base.Constants.PAYMENT_AVAILABLE_BEFORE_CONCERT_START;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class CancelReservationJob implements Job {

    private final ResaleRepository resaleRepository;
    private final SchedulingService schedulingService;
    private final DketResaleService dketResaleService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long resaleId = context.getJobDetail().getJobDataMap().getLong("resaleId");

        log.info("cancelReservationJob: resale [{}]", resaleId);

        Resale resale = resaleRepository.findById(resaleId)
                .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));

        LocalDateTime entry = LocalDateTime.of(resale.getSession().getDate(), resale.getSession().getConcert().getStartTime())
                .minusHours(PAYMENT_AVAILABLE_BEFORE_CONCERT_START);

        if (LocalDateTime.now().isBefore(entry)) {
            resale.cancelReservation();
        } else {
            resale.cancel();
            dketResaleService.cancelResaleBatch(List.of(resaleId));
        }

        String jobName = context.getJobDetail().getKey().getName();
        schedulingService.cancelJob(jobName);
    }

}
