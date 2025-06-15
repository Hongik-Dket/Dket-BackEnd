package com.example.demo.global.infra.scheduling.jobs.event;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.EventRepository;
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

    private final EventRepository eventRepository;
    private final SchedulingService schedulingService;
    private final DketNFTService dketNFTService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long eventId = context.getJobDetail().getJobDataMap().getLong("eventId");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(()->new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        event.setEventStatus(EventStatus.APPLY_CLOSED);

        dketNFTService.recordAllSessionsOnChain(event);

        List<Session> sessions = event.getSessions();
        long emptyCount = sessions.stream()
                .filter(session -> {
                    return session.getApplyList().isEmpty();
                })
                .count();

        if (emptyCount == sessions.size()) {
            dketNFTService.openPublicSaleOnChain(event);
            event.setEventStatus(EventStatus.TICKETED);
            schedulingService.scheduleEventJob(event, OpenPublicJob.class);
        }
    }
}
