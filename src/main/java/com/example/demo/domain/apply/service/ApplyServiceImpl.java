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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private final ApplyRepository applyRepository;
    private final ConcertRepository concertRepository;
    private final SessionRepository sessionRepository;
    private final UserService userService;

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

        // 1. Concert 존재 확인
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CONCERT_NOT_FOUND));

        if (concert.getOrganizer().getId().equals(user.getId())) {
            throw new CustomException(ErrorStatus.APPLY_SELF_HOSTING_NOT_ALLOWED);
        }

        // 2. Session 존재, Concert 일치 여부 확인
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));
        if (!session.getConcert().getId().equals(concertId)) {
            throw new CustomException(ErrorStatus.CONCERT_SESSION_MISMATCH);
        }

        // 3. 공연 상태 및 응모 기간 확인
        if (!concert.getConcertStatus().equals(ConcertStatus.APPLY_OPEN)) {
            throw new CustomException(ErrorStatus.APPLY_INVALID_PERIOD);
        }

        // 4. 중복 응모 방지
        boolean alreadyApplied = applyRepository.existsByUserIdAndSessionId(user.getId(), sessionId);
        if (alreadyApplied) {
            throw new CustomException(ErrorStatus.APPLY_ALREADY_DONE);
        }

        if (!user.isEligibleFor(concert.getAgeLimit())) {
            throw new CustomException(ErrorStatus.APPLY_AGE_RESTRICTED);
        }

        // 5. 저장
        Apply apply = Apply.builder()
                .session(session)
                .user(user)
                .applyStatus(ApplyStatus.APPLIED)
                .build();

        applyRepository.save(apply);
        user.addApply(apply);
        session.addApply(apply);

        return ApplyResponseDTO.builder()
                .applyId(apply.getId())
                .sessionId(sessionId)
                .appliedAt(apply.getCreatedAt())
                .build();
    }
}
