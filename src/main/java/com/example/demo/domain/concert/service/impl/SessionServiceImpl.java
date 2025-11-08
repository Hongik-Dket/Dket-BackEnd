package com.example.demo.domain.concert.service.impl;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.apply.service.ApplicantsSnapshotService;
import com.example.demo.domain.concert.dto.response.EntryCodeDTO;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.concert.repository.ConcertRepository;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.concert.service.SessionService;
import com.example.demo.domain.concert.dto.response.PriceWeiDTO;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.infra.scheduling.jobs.concert.OpenPublicJob;
import com.example.demo.global.infra.scheduling.jobs.session.ClosePaymentJob;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor

public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;
    private final SchedulingService schedulingService;
    private final UserService userService;
    private final TicketRepository ticketRepository;
    private final ConcertRepository concertRepository;

    @Override
    @Transactional
    public void saveWinners(Long sessionId, List<String> winners) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        if (session.isDrawn())
            throw new CustomException(ErrorStatus.SESSION_ALREADY_DRAWN);

        applyRepository.batchUpdateApplyStatusBySessionIdAndWalletAddresses(
                session.getId(), winners, ApplyStatus.SELECTED);

        applyRepository.batchUpdateStatusExceptWallets(
                session.getId(), winners, ApplyStatus.NOT_SELECTED);

        Concert concert = session.getConcert();
        if (concert.getConcertStatus() != ConcertStatus.APPLY_CLOSED) {
            concert.setConcertStatus(ConcertStatus.APPLY_CLOSED);
            schedulingService.scheduleConcertJob(concert, OpenPublicJob.class);
        }
    }

    @Override
    @Transactional
    public void completeDraw(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        session.setIsDrawn();

        if (session.isMinted()) {
            session.setIsBuyable(true);
        }
    }

    @Override
    @Transactional
    public void createSessions(Concert concert) {
        for (LocalDate date = concert.getStartDate(); !date.isAfter(concert.getEndDate()); date = date.plusDays(1)) {
            Session session = Session.builder()
                    .concert(concert)
                    .date(date)
                    .build();

            concert.addSession(session);

            sessionRepository.save(session);
            schedulingService.scheduleSessionJob(session, ClosePaymentJob.class);
        }
    }

    @Override
    public PriceWeiDTO getPriceWei(Long sessionId) {
        User user = userService.getCurrentUser();
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        validateBuyer(session, user);

        return PriceWeiDTO.builder()
                .priceWei(session.getConcert().getPriceWei())
                .sessionId(sessionId)
                .build();
    }

    @Override
    @Transactional
    public EntryCodeDTO getEntryCode(Long concertId, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CONCERT_NOT_FOUND));

        if (!session.getConcert().getId().equals(concert.getId())) {
            throw new CustomException(ErrorStatus.CONCERT_SESSION_MISMATCH);
        }

        User user = userService.getCurrentUser();
        if (!concert.getOrganizer().getId().equals(user.getId())) {
            throw new CustomException(ErrorStatus.CONCERT_ORGANIZER_MISMATCH);
        }

        if (!session.getDate().equals(LocalDate.now())) {
            throw new CustomException(ErrorStatus.SESSION_NOT_TODAY);
        }

        if (session.getEntryCode() == null) {
            String entryCode = String.format("%04d", new Random().nextInt(10000));
            session.setEntryCode(entryCode);
        }

        return EntryCodeDTO.builder()
                .entryCode(session.getEntryCode())
                .build();
    }

    private void validateBuyer(Session session, User user) {
        Concert concert = session.getConcert();

        if (user.getId().equals(concert.getOrganizer().getId())) {
            throw new CustomException(ErrorStatus.CONCERT_ORGANIZER_PURCHASE_FORBIDDEN);
        }

        if (!user.isEligibleFor(concert.getAgeLimit())) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_BUYER);
        }

        if (!session.isBuyable()) {
            throw new CustomException(ErrorStatus.SESSION_CANNOT_BUY);
        }

        if (ticketRepository.existsByUserIdAndSessionId(user.getId(), session.getId())) {
            throw new CustomException(ErrorStatus.TICKET_ALREADY_PAID);
        }

        switch (concert.getConcertStatus()) {
            case APPLY_CLOSED:
                Apply apply = applyRepository.findBySessionIdAndUserId(session.getId(), user.getId())
                        .orElseThrow(() -> new CustomException(ErrorStatus.APPLY_NOT_FOUND));

                if (apply.getApplyStatus() != ApplyStatus.SELECTED)
                    throw new CustomException(ErrorStatus.TICKET_INVALID_BUYER);

                break;

            case TICKETED:
            case IN_PROGRESS:
                break;

            default:
                throw new CustomException(ErrorStatus.SESSION_CANNOT_BUY);
        }
    }
}
