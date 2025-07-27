package com.example.demo.domain.concert.service.impl;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.concert.service.SessionService;
import com.example.demo.domain.concert.dto.response.PriceWeiDTO;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.event.ReadyToMintEvent;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.infra.scheduling.jobs.concert.OpenPublicJob;
import com.example.demo.global.infra.scheduling.jobs.session.ClosePaymentJob;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;
    private final SchedulingService schedulingService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public void saveWinners(Long sessionId, List<String> winners) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        if (session.getIsDrawn())
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

        if(session.getMetadataUploaded()) {
            eventPublisher.publishEvent(new ReadyToMintEvent(session.getId()));
        }
    }

    @Override
    @Transactional
    public void createSessions(Concert concert) {
        for (LocalDate date = concert.getStartDate(); !date.isAfter(concert.getEndDate()); date = date.plusDays(1)) {
            Session session = Session.builder()
                    .concert(concert)
                    .date(date)
                    .isDrawn(false)
                    .metadataUploaded(false)
                    .isBuyable(false)
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

    private void validateBuyer(Session session, User user) {
        Concert concert = session.getConcert();

        if (!user.isEligibleFor(concert.getAgeLimit())) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_BUYER);
        }

        if (!session.getIsBuyable()) {
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
