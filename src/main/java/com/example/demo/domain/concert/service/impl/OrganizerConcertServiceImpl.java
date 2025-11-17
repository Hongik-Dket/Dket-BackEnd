package com.example.demo.domain.concert.service.impl;

import com.example.demo.domain.concert.converter.SessionConverter;
import com.example.demo.domain.concert.dto.request.ConcertUploadDTO;
import com.example.demo.domain.concert.dto.response.ResponseDTO;
import com.example.demo.domain.concert.dto.response.OrganizerSessionInfoDTO;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.ConcertRepository;
import com.example.demo.domain.concert.dto.response.OrganizerConcertDetailDTO;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.concert.service.OrganizerConcertService;
import com.example.demo.domain.concert.service.SessionService;
import com.example.demo.domain.metadata.dto.PhotoCardInfoDTO;
import com.example.demo.domain.metadata.service.PhotoCardService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.base.Constants;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.infra.image.S3UploadService;
import com.example.demo.global.infra.blockchain.service.ExchangeService;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.infra.scheduling.jobs.concert.OpenApplyJob;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.domain.concert.converter.ConcertConverter.*;
import static com.example.demo.domain.metadata.converter.PhotoCardConverter.toPhotoCardInfoDTO;
import static com.example.demo.global.util.StringUtil.normalize;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrganizerConcertServiceImpl implements OrganizerConcertService {

    private final UserService userService;
    private final ConcertRepository concertRepository;
    private final SessionRepository sessionRepository;
    private final S3UploadService s3UploadService;
    private final SchedulingService schedulingService;
    private final DketNFTService dketNFTService;
    private final ExchangeService exchangeService;
    private final PhotoCardService photoCardService;
    private final SessionService sessionService;

    @Override
    public OrganizerConcertDetailDTO getConcertDetailForOrganizer(Long concertId) {
        User user = userService.getCurrentUser();

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CONCERT_NOT_FOUND));

        validateOrganizer(concert, user);

        List<PhotoCardInfoDTO> photoCardInfoDTOList = concert.getPhotoCards().stream()
                .map(photoCard -> {
                    return toPhotoCardInfoDTO(photoCard);
                })
                .collect(Collectors.toList());

        return toOrganizerConcertInfoDTO(concert, photoCardInfoDTOList);
    }

    @Override
    public OrganizerSessionInfoDTO getSessionInfoForOrganizer(Long concertId, Long sessionId) {
        User user = userService.getCurrentUser();

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CONCERT_NOT_FOUND));

        Session session = sessionRepository.findByIdAndConcertId(sessionId, concertId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        validateOrganizer(concert, user);

        int attendeeCount = (int) session.getTicketList().stream()
                .filter(ticket -> ticket.getEnteredAt() != null)
                .count();

        return SessionConverter.toOrganizerSessionInfoDTO(session, attendeeCount);
    }

    @Override
    @Transactional
    public ResponseDTO uploadConcert(
            ConcertUploadDTO request, MultipartFile banner, MultipartFile poster, List<MultipartFile> photocardList) {
        User user = userService.getCurrentUser();

        // Todo: 시연 영상용 더미데이터 사용 위함. 이후 주석 해제 필요
//        validateSchedule(request);

        String bannerUrl = s3UploadService.saveFile(banner);
        String posterUrl = s3UploadService.saveFile(poster);
        BigInteger priceWei = exchangeService.convertKrwToWei(BigDecimal.valueOf(request.getPriceKrw()));

        String titleNorm = normalize(request.getTitle());

        Concert concert = toConcert(request, user, bannerUrl, posterUrl, priceWei, titleNorm);
        concertRepository.save(concert);
        user.addConcert(concert);

        photoCardService.createPhotoCards(concert, photocardList);
        sessionService.createSessions(concert);

        schedulingService.scheduleConcertJob(concert, OpenApplyJob.class);
        concert.setTxHash(dketNFTService.recordConcertOnChain(concert, concert.getSessions()));

        log.info("INSERT   concertId={}", concert.getId());

        return ResponseDTO.builder()
                .concertId(concert.getId())
                .build();
    }

    private void validateOrganizer(Concert concert, User user) {
        if (!concert.getOrganizer().getId().equals(user.getId())) {
            throw new CustomException(ErrorStatus.CONCERT_ORGANIZER_MISMATCH);
        }
    }

    private void validateSchedule(ConcertUploadDTO request) {
        if ((request.getApplyStart().isBefore(LocalDateTime.now()))
            || (!request.getApplyEnd().isAfter(request.getApplyStart()))
            || (request.getApplyEnd().plusDays(Constants.PAYMENT_DEADLINE).withHour(0).withMinute(0)
                .isAfter(LocalDateTime.of(request.getStartDate(), LocalTime.of(0, 0))))
            || (request.getEndDate().isBefore(request.getStartDate())))
        {
            throw new CustomException(ErrorStatus.CONCERT_INVALID_SCHEDULE);
        }
    }
}
