package com.example.demo.global.infra.scheduling;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.global.base.Constants;
import com.example.demo.global.infra.scheduling.dto.SchedulingResponseDTO;
import com.example.demo.global.infra.scheduling.jobs.event.*;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final Scheduler scheduler;
    private final EventRepository eventRepository;

    private void scheduleJob(String jobName, Class<? extends Job> jobClass, LocalDateTime time, Map<String, Object> jobData) {
        try {
            if (!scheduler.isStarted())
                scheduler.start();

            JobDetail jobDetail = buildJobDetail(jobName, jobClass, jobData);
            Trigger trigger = buildJobTrigger(time);

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            handleSchedulerException(e);
        }
    }

    private JobDetail buildJobDetail(String jobName, Class<? extends Job> jobClass, Map<String, Object> jobData) {
        JobDataMap dataMap = new JobDataMap();

        if (jobData != null)
            dataMap.putAll(jobData);

        return JobBuilder.newJob(jobClass)
                .withIdentity(jobName)
                .usingJobData(dataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(LocalDateTime time) {
        ZoneId appZoneId = TimeZone.getDefault().toZoneId();

        LocalDateTime adjustedTime = time.withSecond(0).withNano(0);
        Date startDate = Date.from(adjustedTime.atZone(appZoneId).toInstant());

        Date now = new Date();
        if (startDate.before(now))
            return TriggerBuilder.newTrigger()
                    .startNow()
                    .build();

        return TriggerBuilder.newTrigger()
                .startAt(startDate)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }

    private void handleSchedulerException(SchedulerException e) {
        Throwable cause = e.getCause();
        if (cause instanceof JobPersistenceException) {
            throw new CustomException(ErrorStatus.JOB_STORE_FAILED);
        } else if (cause instanceof JobExecutionException) {
            throw new CustomException(ErrorStatus.JOB_EXECUTION_FAILED);
        } else {
            throw new CustomException(ErrorStatus.JOB_UNKNOWN);
        }
    }

    public void scheduleEventJob(Event event, Class<? extends  Job> jobClass) {
        String jobName = jobClass.getSimpleName();
        LocalDateTime triggerTime = null;

        switch (jobName) {
            case "OpenApplyJob":
                triggerTime = event.getApplyStart();
                break;
            case "CloseApplyJob":
                triggerTime = event.getApplyEnd();
                break;
            case "OpenPublicJob":
                triggerTime = event.getApplyEnd().withHour(0).withMinute(0).withSecond(0).withNano(0)
                        .plusDays(Constants.PAYMENT_DEADLINE);
                break;
            case "StartEventJob":
                triggerTime = LocalDateTime.of(event.getStartDate(), LocalTime.of(0, 0));
                break;
            case "EndEventJob":
                triggerTime = LocalDateTime.of(event.getEndDate().plusDays(1), LocalTime.of(0, 0));
                break;
            default:
                throw new CustomException(ErrorStatus.INVALID_JOB_CLASS);
        }

        jobName += "_" + event.getId();
        scheduleJob(jobName, jobClass, triggerTime, Map.of("eventId", event.getId()));
    }

    public SchedulingResponseDTO getJobKeys() {
        Set<JobKey> jobKeys = new HashSet<>();

        try {
            jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
        } catch (SchedulerException e) {
            handleSchedulerException(e);
        }

        return SchedulingResponseDTO.builder()
                .JobKeys(jobKeys)
                .build();
    }

    public void scheduleAll() {
        try {
            if (!scheduler.isStarted())
                scheduler.start();

            if (!scheduler.getJobKeys(GroupMatcher.anyGroup()).isEmpty())
                return;

            List<Event> eventList = eventRepository.findByEventStatusNotIn(List.of(EventStatus.ENDED));

            for (Event event : eventList) {
                switch (event.getEventStatus()) {
                    case APPLY_NOT_OPENED -> scheduleEventJob(event, OpenApplyJob.class);
                    case APPLY_OPEN -> scheduleEventJob(event, CloseApplyJob.class);
                    case APPLY_CLOSED -> scheduleEventJob(event, OpenPublicJob.class);
                    case TICKETED -> scheduleEventJob(event, StartEventJob.class);
                    case IN_PROGRESS -> scheduleEventJob(event, EndEventJob.class);
                }
            }

        } catch (SchedulerException e) {
            handleSchedulerException(e);
        }
    }

}
