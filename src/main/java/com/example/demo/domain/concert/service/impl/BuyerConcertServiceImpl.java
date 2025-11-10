package com.example.demo.domain.concert.service.impl;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.concert.converter.ConcertConverter;
import com.example.demo.domain.concert.dto.response.BuyerConcertDetailDTO;
import com.example.demo.domain.concert.dto.response.BuyerSessionInfoDTO;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.ConcertRepository;
import com.example.demo.domain.concert.service.BuyerConcertService;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.demo.domain.concert.converter.SessionConverter.toBuyerSessionInfoDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerConcertServiceImpl implements BuyerConcertService {

    private final ConcertRepository concertRepository;
    private final ApplyRepository applyRepository;
    private final TicketRepository ticketRepository;
    private final UserService userService;

    @Override
    public BuyerConcertDetailDTO getConcertDetailForBuyer(Long concertId) {
        User user = userService.getCurrentUser();  // 현재 로그인한 구매자

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CONCERT_NOT_FOUND));

        List<Session> sessions = concert.getSessions();

        List<Long> sessionIds = sessions.stream()
                .map(Session::getId)
                .toList();

        List<Apply> applyList = applyRepository.findByUserIdAndSessionIdIn(user.getId(), sessionIds);
        Map<Long, Apply> applyMap = applyList.stream()
                .collect(Collectors.toMap(apply -> apply.getSession().getId(), Function.identity()));

        List<Ticket> ticketList = ticketRepository.findByUserIdAndSessionIdIn(user.getId(), sessionIds);
        Map<Long, Ticket> ticketMap = ticketList.stream()
                .collect(Collectors.toMap(ticket -> ticket.getSession().getId(), Function.identity()));

        List<BuyerSessionInfoDTO> sessionDTOs = sessions.stream().map(session -> {
            Apply apply = applyMap.get(session.getId());
            ApplyStatus applyStatus = apply != null ? apply.getApplyStatus() : null;

            Ticket ticket = ticketMap.get(session.getId());
            Long ticketId = ticket != null ? ticket.getId() : null;

            boolean buyable = validateBuyer(session, user, apply);

            return toBuyerSessionInfoDTO(session, applyStatus, ticketId, buyable);
        }).toList();

        return ConcertConverter.toBuyerConcertInfoDTO(concert, sessionDTOs);
    }

    private boolean validateBuyer(Session session, User user, Apply apply) {
        Concert concert = session.getConcert();

        if (!user.isEligibleFor(concert.getAgeLimit()) || !session.isBuyable()) {
            return false;
        }

        if (ticketRepository.existsByUserIdAndSessionId(user.getId(), session.getId())) {
            return false;
        }

        switch (concert.getConcertStatus()) {
            case APPLY_CLOSED:
                return apply != null && apply.getApplyStatus() == ApplyStatus.SELECTED;

            case TICKETED:
            case IN_PROGRESS:
                return true;

            default:
                return false;
        }
    }
}
