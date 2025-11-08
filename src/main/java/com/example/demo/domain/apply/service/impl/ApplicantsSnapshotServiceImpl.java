package com.example.demo.domain.apply.service.impl;

import com.example.demo.domain.apply.entity.ApplicantsSnapshot;
import com.example.demo.domain.apply.entity.ApplicantsSnapshotItem;
import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.repository.ApplicantsSnapshotItemRepository;
import com.example.demo.domain.apply.repository.ApplicantsSnapshotRepository;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.apply.service.ApplicantsSnapshotService;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.demo.global.zkp.util.Hexes.toBytesList;
import static com.example.demo.global.zkp.util.Keccak.keccakListHash;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplicantsSnapshotServiceImpl implements ApplicantsSnapshotService {

    private final ApplicantsSnapshotRepository applicantsSnapshotRepository;
    private final ApplyRepository applyRepository;
    private final ApplicantsSnapshotItemRepository applicantsSnapshotItemRepository;

    @Override
    @Transactional
    public ApplicantsSnapshot createSnapshot(Session session) {
        if (applicantsSnapshotRepository.findBySessionId(session.getId()).isPresent()) {
            throw new CustomException(ErrorStatus.SNAPSHOT_ALREADY_EXISTS);
        }

        if (!session.getConcert().getConcertStatus().equals(ConcertStatus.APPLY_CLOSED)) {
            throw new CustomException(ErrorStatus.APPLY_NOT_CLOSED);
        }

        List<Apply> applyList = applyRepository.findAllBySessionIdOrderByIdAsc(session.getId());
        if (applyList.isEmpty()) {
            throw new CustomException(ErrorStatus.APPLY_EMPTY);
        }

        ApplicantsSnapshot snapshot = ApplicantsSnapshot.builder()
                .session(session)
                .listHash("0x")
                .totalCount(0)
                .build();
        applicantsSnapshotRepository.save(snapshot);

        int idx = 0;
        List<String> leaves = new ArrayList<>(applyList.size());
        for (Apply apply : applyList) {
            ApplicantsSnapshotItem item = ApplicantsSnapshotItem.builder()
                    .applicantsSnapshot(snapshot)
                    .ordIndex(idx++)
                    .apply(apply)
                    .build();

            applicantsSnapshotItemRepository.save(item);
            leaves.add(apply.getLeafHex());
            snapshot.addItem(item);
        }

        String listHash = keccakListHash(leaves);
        snapshot.setListHash(listHash);
        snapshot.setTotalCount(leaves.size());

        return snapshot;
    }

    @Override
    public List<byte[]> buildLeavesForDraw(Session session) {
        ApplicantsSnapshot snapshot = applicantsSnapshotRepository.findBySessionId(session.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SNAPSHOT_NOT_FOUND));

        List<String> leaves = applicantsSnapshotItemRepository
                .findAllByApplicantsSnapshotIdOrderByOrdIndexAsc(snapshot.getId())
                .stream().map(i -> i.getApply().getLeafHex()).toList();

        if (leaves.size() != snapshot.getTotalCount()) {
            throw new CustomException(ErrorStatus.SNAPSHOT_INVALID);
        }

        return toBytesList(leaves);
    }

}