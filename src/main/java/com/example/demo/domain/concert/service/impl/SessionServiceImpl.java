package com.example.demo.domain.concert.service.impl;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.concert.service.SessionService;
import com.example.demo.domain.concert.dto.response.PriceWeiAndChallengeDTO;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.infra.scheduling.jobs.session.ClosePaymentJob;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.zkp.signature.entity.Challenge;
import com.example.demo.global.zkp.signature.enums.ChallengePurpose;
import com.example.demo.global.zkp.signature.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;
    private final SchedulingService schedulingService;
    private final UserService userService;
    private final TicketRepository ticketRepository;
    private final ChallengeService challengeService;

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
    @Transactional
    public PriceWeiAndChallengeDTO getPriceWeiAndChallenge(Long sessionId) {
        User user = userService.getCurrentUser();
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        validateBuyer(session, user);

        String challengeId = null;
        String challenge = null;
        if (session.getConcert().getConcertStatus().equals(ConcertStatus.APPLY_CLOSED)) {
            Challenge c = challengeService.issueChallenge(user.getId(), session.getId(), ChallengePurpose.WIN_PROOF);
            challengeId = c.getId();
            challenge = c.getMessage();
        }

        return PriceWeiAndChallengeDTO.builder()
                .sessionId(session.getId())
                .priceWei(session.getConcert().getPriceWei())
                .challengeId(challengeId)
                .challenge(challenge)
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
