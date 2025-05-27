package com.example.demo.global.infra.scheduling.jobs.event;

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
public class CloseApplyJob implements Job {

    private final EventRepository eventRepository;
    private final BlockchainService blockchainService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long eventId = context.getJobDetail().getJobDataMap().getLong("eventId");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(()->new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        event.setEventStatus(EventStatus.APPLY_CLOSED);

        blockchainService.createSessionOnChain(event);
    }
}
