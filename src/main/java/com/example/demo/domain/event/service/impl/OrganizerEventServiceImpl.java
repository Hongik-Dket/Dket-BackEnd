package com.example.demo.domain.event.service.impl;

import com.example.demo.domain.event.dto.request.EventUploadDTO;
import com.example.demo.domain.event.dto.response.ResponseDTO;
import com.example.demo.domain.event.dto.response.SessionInfoDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.domain.event.dto.response.EventInfoDTO;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.event.service.OrganizerEventService;
import com.example.demo.domain.metadata.service.PhotoCardService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.base.Constants;
import com.example.demo.global.infra.awsS3.S3UploadService;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.infra.blockchain.service.ExchangeService;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.infra.scheduling.jobs.event.OpenApplyJob;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.example.demo.domain.event.converter.EventConverter.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrganizerEventServiceImpl implements OrganizerEventService {

    private final UserService userService;
    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;
    private final S3UploadService s3UploadService;
    private final SchedulingService schedulingService;
    private final DketNFTService dketNFTService;
    private final ExchangeService exchangeService;
    private final PhotoCardService photoCardService;

    @Override
    public EventInfoDTO getEventInfoForOrganizer(Long eventId) {
        User user = userService.getCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        if (!event.getOrganizer().equals(user))
            throw new CustomException(ErrorStatus.EVENT_ORGANIZER_MISMATCH);

        return toEventInfoDTO(event);
    }

    @Override
    public SessionInfoDTO getSessionInfoForOrganizer(Long eventId, Long sessionId) {
        User user = userService.getCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        if (!event.getOrganizer().equals(user))
            throw new CustomException(ErrorStatus.EVENT_ORGANIZER_MISMATCH);

        if (!session.getEvent().equals(event))
            throw new CustomException(ErrorStatus.EVENT_SESSION_MISMATCH);

        int attendeeCount = (int) session.getTicketList().stream()
                .filter(ticket -> ticket.getEnteredAt() != null)
                .count();

        return toSessionInfoDTO(session, attendeeCount);
    }

    @Override
    @Transactional
    public ResponseDTO uploadEvent(
            EventUploadDTO request, MultipartFile banner, MultipartFile poster, List<MultipartFile> photocardList) {
        User user = userService.getCurrentUser();

        validateSchedule(request);

        String bannerUrl = s3UploadService.saveFile(banner);
        String posterUrl = s3UploadService.saveFile(poster);
        BigInteger priceWei = exchangeService.convertKrwToWei(BigDecimal.valueOf(request.getPriceKrw()));

        Event event = toEvent(request, user, bannerUrl, posterUrl, priceWei);

        event.setEventStatus(EventStatus.APPLY_NOT_OPENED);
        eventRepository.save(event);

        photoCardService.createPhotoCards(event, photocardList);

        for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
            Session session = Session.builder()
                    .event(event)
                    .date(date)
                    .isDrawn(false)
                    .metadataUploaded(false)
                    .build();

            event.addSession(session);
        }

        user.addEvent(event);

        schedulingService.scheduleEventJob(event, OpenApplyJob.class);
        event.setTxHash(dketNFTService.recordEventOnChain(event));

        return ResponseDTO.builder()
                .eventId(event.getId())
                .build();
    }

    private void validateSchedule(EventUploadDTO request) {
        if (request.getApplyStart().isBefore(LocalDateTime.now()))
            throw new CustomException(ErrorStatus.EVENT_INVALID_SCHEDULE);

        if (!request.getApplyEnd().isAfter(request.getApplyStart()))
            throw new CustomException(ErrorStatus.EVENT_INVALID_SCHEDULE);

        if (request.getApplyEnd().plusDays(Constants.PAYMENT_DEADLINE).withHour(0).withMinute(0)
                .isAfter(LocalDateTime.of(request.getStartDate(), LocalTime.of(0, 0))))
            throw new CustomException(ErrorStatus.EVENT_INVALID_SCHEDULE);

        if (request.getEndDate().isBefore(request.getStartDate()))
            throw new CustomException(ErrorStatus.EVENT_INVALID_SCHEDULE);
    }
}
