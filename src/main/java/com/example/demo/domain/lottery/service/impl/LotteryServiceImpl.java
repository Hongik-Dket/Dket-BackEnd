package com.example.demo.domain.lottery.service.impl;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.lottery.entity.ApplicantsSnapshot;
import com.example.demo.domain.lottery.entity.ApplicantsSnapshotItem;
import com.example.demo.domain.lottery.entity.WinnersAggregate;
import com.example.demo.domain.lottery.entity.WinnersEvent;
import com.example.demo.domain.lottery.repository.ApplicantsSnapshotItemRepository;
import com.example.demo.domain.lottery.repository.ApplicantsSnapshotRepository;
import com.example.demo.domain.lottery.repository.WinnersAggregateRepository;
import com.example.demo.domain.lottery.repository.WinnersEventRepository;
import com.example.demo.domain.lottery.service.ApplicantsSnapshotService;
import com.example.demo.domain.lottery.service.LotteryService;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.zkp.poseidon.PoseidonMerkleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.global.util.Hexes.hexToBytes;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {

    private final SessionRepository sessionRepository;
    private final WinnersEventRepository winnersEventRepository;
    private final ApplicantsSnapshotRepository applicantsSnapshotRepository;
    private final ApplicantsSnapshotItemRepository applicantsSnapshotItemRepository;
    private final ApplyRepository applyRepository;
    private final PoseidonMerkleService poseidonMerkleService;
    private final WinnersAggregateRepository winnersAggregateRepository;
    private final DketNFTService dketNFTService;
    private final ApplicantsSnapshotService applicantsSnapshotService;

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

    @Override
    @Transactional
    public void saveWinners(Long sessionId, int count, List<BigInteger> indices,
                               String txHash, Long blockNumber, Integer logIndex) {

        if (winnersEventRepository.existsByTxHashAndLogIndex(txHash, logIndex)) {
            return;
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        ApplicantsSnapshot snapshot = applicantsSnapshotRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SNAPSHOT_NOT_FOUND));

        List<ApplicantsSnapshotItem> items =
                applicantsSnapshotItemRepository.findAllByApplicantsSnapshotIdOrderByOrdIndexAsc(snapshot.getId());

        int applies = items.size();
        List<Integer> idxList = indices.stream().map(BigInteger::intValueExact).toList();

        if (count != idxList.size()) {
            throw new CustomException(ErrorStatus.LOTTERY_INVALID_INDEX);
        }
        for (int idx : idxList) {
            if (idx < 0 || idx >= applies) {
                throw new CustomException(ErrorStatus.LOTTERY_INVALID_INDEX);
            }
        }

        List<Long> winnerIds = new ArrayList<>();
        for (int idx : idxList) {
            ApplicantsSnapshotItem it = items.get(idx);
            winnerIds.add(it.getApply().getId());
        }
        applyRepository.batchUpdateApplyStatusByIds(winnerIds, ApplyStatus.APPLIED, ApplyStatus.SELECTED);

        WinnersEvent ev = WinnersEvent.builder()
                .sessionId(sessionId)
                .accepted(count)
                .blockNumber(blockNumber)
                .txHash(txHash)
                .logIndex(logIndex)
                .winnerIndices(idxList)
                .build();
        winnersEventRepository.save(ev);


        List<String> winnerLeafHexes = applicantsSnapshotItemRepository.findWinnerLeafHexes(sessionId);
        String poseidonRoot = poseidonMerkleService.rootHex(winnerLeafHexes);

        WinnersAggregate aggregate = winnersAggregateRepository.findBySessionId(sessionId)
                .orElse(WinnersAggregate.builder().sessionId(sessionId).build());

        aggregate.update(winnerLeafHexes.size(), poseidonRoot, blockNumber);
        winnersAggregateRepository.save(aggregate);

        int winners = aggregate.getWinnersCount();
        if (applies == winners || session.getConcert().getCapacity() == winners) {
            List<Long> winnerIdList = applyRepository.findIdsBySessionIdAndApplyStatus(sessionId, ApplyStatus.SELECTED);
            applyRepository.batchUpdateApplyStatusExceptIdsBySessionId(
                    sessionId, winnerIdList, ApplyStatus.APPLIED, ApplyStatus.NOT_SELECTED);

            byte[] root = hexToBytes(poseidonRoot);

            session.setIsDrawn(root);
            if (session.isMinted()) {
                session.setIsBuyable(true);
            }

            dketNFTService.finalizeWinnersRoot(session);
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
}
