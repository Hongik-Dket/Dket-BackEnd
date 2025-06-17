package com.example.demo.domain.event.service.impl;

import com.example.demo.domain.event.converter.SessionConverter;
import com.example.demo.domain.event.dto.request.EventUploadDTO;
import com.example.demo.domain.event.dto.response.ResponseDTO;
import com.example.demo.domain.event.dto.response.OrganizerSessionInfoDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.domain.event.dto.response.OrganizerEventDetailDTO;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.event.service.OrganizerEventService;
import com.example.demo.domain.event.service.SessionService;
import com.example.demo.domain.metadata.dto.PhotoCardInfoDTO;
import com.example.demo.domain.metadata.service.PhotoCardService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.base.Constants;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.infra.image.S3UploadService;
import com.example.demo.global.infra.blockchain.service.ExchangeService;
import com.example.demo.global.infra.ipfs.PinataService;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.domain.event.converter.EventConverter.*;
import static com.example.demo.domain.metadata.converter.PhotoCardConverter.toPhotoCardInfoDTO;

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
    private final PinataService pinataService;
    private final SessionService sessionService;

    @Override
    public OrganizerEventDetailDTO getEventDetailForOrganizer(Long eventId) {
        User user = userService.getCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        validateOrganizer(event, user);

        List<PhotoCardInfoDTO> photoCardInfoDTOList = event.getPhotoCards().stream()
                .map(photoCard -> {
                    String url = pinataService.cidToHttp(photoCard.getCid());
                    return toPhotoCardInfoDTO(photoCard, url);
                })
                .collect(Collectors.toList());

        return toOrganizerEventInfoDTO(event, photoCardInfoDTOList);
    }

    @Override
    public OrganizerSessionInfoDTO getSessionInfoForOrganizer(Long eventId, Long sessionId) {
        User user = userService.getCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.EVENT_NOT_FOUND));

        Session session = sessionRepository.findByIdAndEventId(sessionId, eventId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        validateOrganizer(event, user);

        int attendeeCount = (int) session.getTicketList().stream()
                .filter(ticket -> ticket.getEnteredAt() != null)
                .count();

        return SessionConverter.toOrganizerSessionInfoDTO(session, attendeeCount);
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
        eventRepository.save(event);
        user.addEvent(event);

        photoCardService.createPhotoCards(event, photocardList);
        sessionService.createSessions(event);

        schedulingService.scheduleEventJob(event, OpenApplyJob.class);
        event.setTxHash(dketNFTService.recordEventOnChain(event));

        return ResponseDTO.builder()
                .eventId(event.getId())
                .build();
    }

    private void validateOrganizer(Event event, User user) {
        if (!event.getOrganizer().getId().equals(user.getId())) {
            throw new CustomException(ErrorStatus.EVENT_ORGANIZER_MISMATCH);
        }
    }

    private void validateSchedule(EventUploadDTO request) {
        if ((request.getApplyStart().isBefore(LocalDateTime.now()))
            || (!request.getApplyEnd().isAfter(request.getApplyStart()))
            || (request.getApplyEnd().plusDays(Constants.PAYMENT_DEADLINE).withHour(0).withMinute(0)
                .isAfter(LocalDateTime.of(request.getStartDate(), LocalTime.of(0, 0))))
            || (request.getEndDate().isBefore(request.getStartDate())))
        {
            throw new CustomException(ErrorStatus.EVENT_INVALID_SCHEDULE);
        }
    }
}
