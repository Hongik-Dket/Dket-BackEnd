package com.example.demo.global.infra.scheduling.jobs.event;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.apply.service.ApplyService;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.global.infra.blockchain.BlockchainService;
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

    private final EventRepository eventRepository;
    private final ApplyService applyService;
    private final BlockchainService blockchainService;
    private final SchedulingService schedulingService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long eventId = context.getJobDetail().getJobDataMap().getLong("eventId");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(()->new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        event.setEventStatus(EventStatus.TICKETED);

        blockchainService.openPublicSaleOnChain(event);
        applyService.cancelWinnerTickets(event);

        schedulingService.scheduleEventJob(event, StartEventJob.class);
    }
}
