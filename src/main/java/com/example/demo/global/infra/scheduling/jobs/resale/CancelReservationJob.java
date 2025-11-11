package com.example.demo.global.infra.scheduling.jobs.resale;

import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.repository.ResaleRepository;
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

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class CancelReservationJob implements Job {

    private final ResaleRepository resaleRepository;
    private final SchedulingService schedulingService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long resaleId = context.getJobDetail().getJobDataMap().getLong("resaleId");

        log.info("cancelReservationJob: resale [{}]", resaleId);

        Resale resale = resaleRepository.findById(resaleId)
                .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));

        resale.cancelReservation();

        String jobName = context.getJobDetail().getKey().getName();
        schedulingService.cancelJob(jobName);
    }

}
