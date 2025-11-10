package com.example.demo.domain.lottery.service.impl;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.lottery.entity.ApplicantsSnapshot;
import com.example.demo.domain.lottery.service.ApplicantsSnapshotService;
import com.example.demo.domain.lottery.service.LotteryOnChainService;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LotteryOnChainServiceImpl implements LotteryOnChainService {

    private final ApplicantsSnapshotService applicantsSnapshotService;
    private final DketNFTService dketNFTService;
    private final SessionRepository sessionRepository;

    @Override
    public void commitApplicants(Session session) {
        ApplicantsSnapshot snapshot = applicantsSnapshotService.createSnapshot(session);
        dketNFTService.setApplicantsListCommitment(session, snapshot);
    }

    @Override
    public void drawWinners(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        List<byte[]> leaves = applicantsSnapshotService.buildLeavesForDraw(session);

        int total = Math.min(session.getConcert().getCapacity(), session.getApplyList().size());
        int count = Math.min(total, 100);
        int batch = total / count;

        for (int i = 0; i < batch; i++) {
            dketNFTService.drawWinnersOnChain(session, count, leaves);
        }

        if (total % count > 0) {
            dketNFTService.drawWinnersOnChain(session, total % count, leaves);
        }
    }
}
