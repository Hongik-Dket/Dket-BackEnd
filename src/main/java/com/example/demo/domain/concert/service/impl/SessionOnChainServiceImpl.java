package com.example.demo.domain.concert.service.impl;

import com.example.demo.domain.apply.entity.ApplicantsSnapshot;
import com.example.demo.domain.apply.service.ApplicantsSnapshotService;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.concert.service.SessionOnChainService;
import com.example.demo.global.event.ReadyToDraw;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SessionOnChainServiceImpl implements SessionOnChainService {

    private final ApplicantsSnapshotService applicantsSnapshotService;
    private final DketNFTService dketNFTService;
    private final SessionRepository sessionRepository;

    @Override
    public void commitApplicants(Session session) {
        ApplicantsSnapshot snapshot = applicantsSnapshotService.createSnapshot(session);
        dketNFTService.setApplicantsListCommitment(session, snapshot);
    }

    @EventListener
    public void drawWinners(ReadyToDraw event) {
        Session session = sessionRepository.findById(event.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        List<byte[]> leaves = applicantsSnapshotService.buildLeavesForDraw(session);

        int applies = session.getApplyList().size();
        int count = 100;
        int batch = applies / count;

        for (int i = 0; i < batch; i++) {
            dketNFTService.drawWinnersOnChain(session, count, leaves);
        }

        if (applies % count > 0) {
            dketNFTService.drawWinnersOnChain(session, applies % count, leaves);
        }
    }

}
