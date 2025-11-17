package com.example.demo.domain.apply.service;

import com.example.demo.domain.apply.dto.ApplyResponseDTO;
import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.concert.repository.ConcertRepository;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.zkp.poseidon.Poseidon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.demo.global.base.Constants.APPLY_TAG;
import static com.example.demo.global.util.Hexes.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private final ApplyRepository applyRepository;
    private final ConcertRepository concertRepository;
    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final Poseidon poseidon;

    @Override
    @Transactional
    public void cancelWinnerTickets(Concert concert) {

        List<Long> sessionIds = concertRepository.findSessionIdsByConcertId(concert.getId());

        for (Long sessionId : sessionIds) {
            applyRepository.batchUpdateApplyStatusBySessionId(sessionId, ApplyStatus.SELECTED, ApplyStatus.CANCELED);
        }

    }

    @Override
    @Transactional
    public ApplyResponseDTO applyToSession(Long concertId, Long sessionId) {
        User user = userService.getCurrentUser();

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CONCERT_NOT_FOUND));

        if (concert.getOrganizer().getId().equals(user.getId())) {
            throw new CustomException(ErrorStatus.APPLY_SELF_HOSTING_NOT_ALLOWED);
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));
        if (!session.getConcert().getId().equals(concertId)) {
            throw new CustomException(ErrorStatus.CONCERT_SESSION_MISMATCH);
        }

        if (!concert.getConcertStatus().equals(ConcertStatus.APPLY_OPEN)
                || concert.getApplyEnd().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorStatus.APPLY_INVALID_PERIOD);
        }

        boolean alreadyApplied = applyRepository.existsByUserIdAndSessionId(user.getId(), sessionId);
        if (alreadyApplied) {
            throw new CustomException(ErrorStatus.APPLY_ALREADY_DONE);
        }

        if (!user.isEligibleFor(concert.getAgeLimit())) {
            throw new CustomException(ErrorStatus.APPLY_AGE_RESTRICTED);
        }

        BigInteger ic = new BigInteger(1, hexToBytes(user.getIcCommitment()));
        BigInteger sid = BigInteger.valueOf(sessionId);
        BigInteger h = poseidon.hash(ic, sid);
        h = poseidon.hash(h, APPLY_TAG);

        String leafHex = to0xHex(bigIntToBe32(h));

        Apply apply = Apply.builder()
                .session(session)
                .user(user)
                .applyStatus(ApplyStatus.APPLIED)
                .leafHex(leafHex)
                .build();

        applyRepository.save(apply);
        user.addApply(apply);
        session.addApply(apply);

        log.info("INSERT   applyId={}, sessionId={}, userId={}", apply.getId(), session.getId(), user.getId());

        return ApplyResponseDTO.builder()
                .applyId(apply.getId())
                .sessionId(sessionId)
                .appliedAt(apply.getCreatedAt())
                .build();
    }
}
