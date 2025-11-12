package com.example.demo.domain.proof.service;

import com.example.demo.domain.proof.dto.response.ProofDTO;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.lottery.entity.ApplicantsSnapshotItem;
import com.example.demo.domain.lottery.repository.ApplicantsSnapshotItemRepository;
import com.example.demo.domain.proof.dto.request.WinProofAuthDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.zkp.win.WinProverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProofServiceImpl implements ProofService {

    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final ApplicantsSnapshotItemRepository applicantsSnapshotItemRepository;
    private final WinProverService winProverService;

    @Override
    public ProofDTO issueWinProof(WinProofAuthDTO request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        User user = userService.getCurrentUser();

        ApplicantsSnapshotItem item = applicantsSnapshotItemRepository
                .findBySessionIdAndUserId(session.getId(), user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SNAPSHOT_ITEM_NOT_FOUND));

        List<String> winnerLeafHexes = applicantsSnapshotItemRepository.findWinnerLeafHexes(session.getId());

        int idx = -1;
        for (int i = 0; i < winnerLeafHexes.size(); i++) {
            if (winnerLeafHexes.get(i).equalsIgnoreCase(item.getApply().getLeafHex())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            throw new CustomException(ErrorStatus.ZKP_NOT_A_WINNER);
        }

        WinProverService.WinProof proof = winProverService.prove(
                session.getId(),
                idx,
                user.getIcCommitment()
        );

        return ProofDTO.builder()
                .proof(proof.getProof())
                .nullifier(proof.getPaymentNullifierHex())
                .build();
    }

}
