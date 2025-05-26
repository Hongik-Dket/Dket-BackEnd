package com.example.demo.global.infra.scheduling.jobs.event;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.EventRepository;
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
    private final ApplyRepository applyRepository;
    private final SchedulingService schedulingService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long eventId = context.getJobDetail().getJobDataMap().getLong("eventId");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(()->new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        event.setEventStatus(EventStatus.TICKETED);

        applyRepository.batchUpdateApplyStatus(ApplyStatus.SELECTED, ApplyStatus.CANCELED);

        // Todo: 온체인 선착순 판매 전환

        schedulingService.scheduleEventJob(event, StartEventJob.class);
    }
}
