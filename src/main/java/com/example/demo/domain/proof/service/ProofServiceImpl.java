package com.example.demo.domain.proof.service;

import com.example.demo.domain.proof.dto.response.ProofDTO;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.lottery.entity.ApplicantsSnapshotItem;
import com.example.demo.domain.lottery.repository.ApplicantsSnapshotItemRepository;
import com.example.demo.domain.proof.dto.request.ProofAuthDTO;
import com.example.demo.domain.proof.dto.response.ProofQrCodeDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.zkp.signature.entity.Challenge;
import com.example.demo.global.zkp.signature.enums.ChallengePurpose;
import com.example.demo.global.zkp.signature.repository.ChallengeRepository;
import com.example.demo.global.zkp.signature.service.SecureEnclaveVerifier;
import com.example.demo.global.zkp.proof.ProverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProofServiceImpl implements ProofService {

    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final ApplicantsSnapshotItemRepository applicantsSnapshotItemRepository;
    private final ProverService proverService;
    private final ChallengeRepository challengeRepository;

    @Override
    @Transactional
    public ProofDTO issueWinProof(ProofAuthDTO request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        User user = userService.getCurrentUser();

        ApplicantsSnapshotItem item = applicantsSnapshotItemRepository
                .findBySessionIdAndUserId(session.getId(), user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SNAPSHOT_ITEM_NOT_FOUND));

        if (!user.getPublicKey().equals(request.getPublicKey())) {
            throw new CustomException(ErrorStatus.SIG_PUBKEY_MISMATCH_USER);
        }

        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SIG_CHALLENGE_NOT_FOUND));

        if (!challenge.getUserId().equals(user.getId())
                || challenge.getExpiresAt().isBefore(LocalDateTime.now())
                || !challenge.getSessionId().equals(session.getId())
                || !challenge.getPurpose().equals(ChallengePurpose.WIN_PROOF)
                || challenge.isUsed()
        ) {
            throw new CustomException(ErrorStatus.SIG_INVALID_CHALLENGE);
        }

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

        if (!SecureEnclaveVerifier.verify(challenge.getMessage(), request.getSignature(), user.getPublicKey())) {
            throw new CustomException(ErrorStatus.SIG_VERIFY_FAILED);
        }

        log.info("Starting ProverService.proveWin... : session [{}], user [{}]", session.getId(), user.getId());
        ProverService.Proof proof = proverService.proveWin(
                session.getId(),
                idx,
                user.getIcCommitment()
        );

        challenge.setUsed();

        return ProofDTO.builder()
                .proof(proof.getProof())
                .nullifier(proof.getPublicSignals().get(2))
                .build();
    }

    public ProofQrCodeDTO issueOwnProof(ProofAuthDTO request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        User user = userService.getCurrentUser();

        return null;
    }

}
