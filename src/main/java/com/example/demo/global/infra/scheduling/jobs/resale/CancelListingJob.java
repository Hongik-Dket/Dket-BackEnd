package com.example.demo.global.infra.scheduling.jobs.resale;

import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class CancelListingJob implements Job {

    private final ResaleRepository resaleRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long resaleId = context.getJobDetail().getJobDataMap().getLong("resaleId");

        Resale resale;
        try {
            resale = resaleRepository.findByIdForUpdate(resaleId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new CustomException(ErrorStatus.RESALE_CONFLICT);
        }

        resale.cancelListing();
    }
}
