package com.example.demo.global.infra.scheduling;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.concert.repository.ConcertRepository;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.global.base.Constants;
import com.example.demo.global.infra.scheduling.dto.SchedulingResponseDTO;
import com.example.demo.global.infra.scheduling.jobs.concert.*;
import com.example.demo.global.infra.scheduling.jobs.resale.CancelListingJob;
import com.example.demo.global.infra.scheduling.jobs.resale.CancelReservationJob;
import com.example.demo.global.infra.scheduling.jobs.session.ClosePaymentJob;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final Scheduler scheduler;
    private final ConcertRepository concertRepository;
    private final SessionRepository sessionRepository;
    private final ResaleRepository resaleRepository;

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
        log.error("스케줄링 실패", e);

        Throwable cause = e.getCause();
        if (cause instanceof JobPersistenceException) {
            throw new CustomException(ErrorStatus.JOB_STORE_FAILED);
        } else if (cause instanceof JobExecutionException) {
            throw new CustomException(ErrorStatus.JOB_EXECUTION_FAILED);
        } else {
            throw new CustomException(ErrorStatus.JOB_UNKNOWN);
        }
    }

    public void scheduleConcertJob(Concert concert, Class<? extends  Job> jobClass) {
        String jobName = jobClass.getSimpleName();
        LocalDateTime triggerTime = null;

        switch (jobName) {
            case "OpenApplyJob":
                triggerTime = concert.getApplyStart();
                break;
            case "CloseApplyJob":
                triggerTime = concert.getApplyEnd();
                break;
            case "OpenPublicJob":
                triggerTime = concert.getApplyEnd().withHour(0).withMinute(0).withSecond(0).withNano(0)
                        .plusDays(Constants.PAYMENT_DEADLINE);
                break;
            case "StartConcertJob":
                triggerTime = LocalDateTime.of(concert.getStartDate(), LocalTime.of(0, 0));
                break;
            case "EndConcertJob":
                triggerTime = LocalDateTime.of(concert.getEndDate().plusDays(1), LocalTime.of(0, 0));
                break;
            default:
                throw new CustomException(ErrorStatus.INVALID_JOB_CLASS);
        }

        jobName += "_" + concert.getId();
        scheduleJob(jobName, jobClass, triggerTime, Map.of("concertId", concert.getId()));
    }

    public void scheduleSessionJob(Session session, Class<? extends  Job> jobClass) {
        String jobName = jobClass.getSimpleName();
        LocalDateTime triggerTime = null;

        switch (jobName) {
            case "ClosePaymentJob":
                triggerTime = LocalDateTime.of(session.getDate(), session.getConcert().getStartTime())
                        .minusHours(Constants.PAYMENT_AVAILABLE_BEFORE_CONCERT_START);
                break;
            default:
                throw new CustomException(ErrorStatus.INVALID_JOB_CLASS);
        }

        jobName += "_" + session.getId();
        scheduleJob(jobName, jobClass, triggerTime, Map.of("sessionId", session.getId()));
    }

    public void scheduleResaleJob(Resale resale, Class<? extends  Job> jobClass) {
        String jobName = jobClass.getSimpleName();
        LocalDateTime triggerTime = null;

        switch (jobName) {
            case "CancelListingJob":
                triggerTime = LocalDateTime.now().plusMinutes(Constants.ONCHAIN_TIMEOUT);
                break;
            case "CancelReservationJob":
                triggerTime = resale.getReservationExpiresAt();
                break;
            default:
                throw new CustomException(ErrorStatus.INVALID_JOB_CLASS);
        }

        jobName += "_" + resale.getId();
        scheduleJob(jobName, jobClass, triggerTime, Map.of("resaleId", resale.getId()));
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

            List<Concert> concertList = concertRepository.findByConcertStatusNotIn(List.of(ConcertStatus.ENDED));
            for (Concert concert : concertList) {
                switch (concert.getConcertStatus()) {
                    case APPLY_NOT_OPENED -> scheduleConcertJob(concert, OpenApplyJob.class);
                    case APPLY_OPEN -> scheduleConcertJob(concert, CloseApplyJob.class);
                    case APPLY_CLOSED -> scheduleConcertJob(concert, OpenPublicJob.class);
                    case TICKETED -> scheduleConcertJob(concert, StartConcertJob.class);
                    case IN_PROGRESS -> scheduleConcertJob(concert, EndConcertJob.class);
                }
            }

            List<Session> sessionList = sessionRepository.findByIsBuyableTrueAndIsDrawnTrueAndMetadataUploadedTrue();
            for (Session session : sessionList) {
                scheduleSessionJob(session, ClosePaymentJob.class);
            }

            List<Resale> resaleList = resaleRepository.findByResaleStatusIn(
                    EnumSet.of(ResaleStatus.LISTING, ResaleStatus.RESERVED, ResaleStatus.PENDING)
            );
            for (Resale resale : resaleList) {
                switch (resale.getResaleStatus()) {
                    case LISTING:
                        scheduleResaleJob(resale, CancelListingJob.class);
                        break;
                    case RESERVED:
                    case PENDING:
                        scheduleResaleJob(resale, CancelReservationJob.class);
                }
            }

        } catch (SchedulerException e) {
            handleSchedulerException(e);
        }
    }

    public void cancelJob(String jobName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName);
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName);

            if (scheduler.checkExists(jobKey)) {
                scheduler.unscheduleJob(triggerKey);
                scheduler.deleteJob(jobKey);
            }

        } catch (SchedulerException e) {
            throw new CustomException(ErrorStatus.JOB_CANCEL_FAILED);
        }
    }

}
