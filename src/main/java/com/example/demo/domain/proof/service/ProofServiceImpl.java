package com.example.demo.domain.proof.service;

import com.example.demo.domain.ownership.entity.Ownership;
import com.example.demo.domain.ownership.repository.OwnershipRepository;
import com.example.demo.domain.proof.dto.response.ChallengeDTO;
import com.example.demo.domain.proof.dto.response.ProofDTO;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.lottery.entity.ApplicantsSnapshotItem;
import com.example.demo.domain.lottery.repository.ApplicantsSnapshotItemRepository;
import com.example.demo.domain.proof.dto.request.ProofAuthDTO;
import com.example.demo.domain.proof.dto.response.ProofQrCodeDTO;
import com.example.demo.domain.proof.entity.OwnProof;
import com.example.demo.domain.proof.repository.OwnProofRepository;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.infra.image.QrCodeGenerator;
import com.example.demo.global.infra.image.S3UploadService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.zkp.signature.entity.Challenge;
import com.example.demo.global.zkp.signature.enums.ChallengePurpose;
import com.example.demo.global.zkp.signature.repository.ChallengeRepository;
import com.example.demo.global.zkp.signature.service.ChallengeService;
import com.example.demo.global.zkp.signature.service.SecureEnclaveVerifier;
import com.example.demo.global.zkp.proof.ProverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

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
    private final OwnershipRepository ownershipRepository;
    private final TicketRepository ticketRepository;
    private final ResaleRepository resaleRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final S3UploadService s3UploadService;
    private final ChallengeService challengeService;
    private final OwnProofRepository ownProofRepository;
    private final ObjectMapper objectMapper;

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

    @Override
    @Transactional
    public ChallengeDTO issueChallenge(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();

        Challenge challenge = challengeService.issueChallenge(
                user.getId(), ticket.getSession().getId(), ChallengePurpose.OWN_PROOF);

        return ChallengeDTO.builder()
                .challengeId(challenge.getId())
                .challenge(challenge.getMessage())
                .build();
    }

    @Override
    @Transactional
    public ProofQrCodeDTO issueOwnProof(ProofAuthDTO request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        if (!session.getDate().equals(LocalDate.now())) {
            throw new CustomException(ErrorStatus.SESSION_NOT_TODAY);
        }

        User user = userService.getCurrentUser();

        Ticket ticket = ticketRepository.findByUserIdAndSessionId(user.getId(), session.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        if (resaleRepository.existsByTicketIdAndResaleStatusIn(ticket.getId(), EnumSet.of(ResaleStatus.PENDING))) {
            throw new CustomException(ErrorStatus.TICKET_RESALE_PENDING);
        }

        if (!user.getPublicKey().equals(request.getPublicKey())) {
            throw new CustomException(ErrorStatus.SIG_PUBKEY_MISMATCH_USER);
        }

        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SIG_CHALLENGE_NOT_FOUND));

        if (!challenge.getUserId().equals(user.getId())
                || challenge.getExpiresAt().isBefore(LocalDateTime.now())
                || challenge.getSessionId() == null
                || !challenge.getSessionId().equals(session.getId())
                || !challenge.getPurpose().equals(ChallengePurpose.OWN_PROOF)
                || challenge.isUsed()
        ) {
            throw new CustomException(ErrorStatus.SIG_INVALID_CHALLENGE);
        }

        Ownership ownership = ownershipRepository.findBySessionIdAndUserId(session.getId(), user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.OWN_NOT_FOUND));

        if (!SecureEnclaveVerifier.verify(challenge.getMessage(), request.getSignature(), user.getPublicKey())) {
            throw new CustomException(ErrorStatus.SIG_VERIFY_FAILED);
        }

        List<Ownership> ownerships = ownershipRepository.findAllByOwnersAggregateIdOrderByOrdIndexAsc(
                ownership.getOwnersAggregate().getId());

        int idx = -1;
        for (int i = 0; i < ownerships.size(); i++) {
            if (ownerships.get(i).equals(ownership)) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            throw new CustomException(ErrorStatus.ZKP_NOT_AN_OWNER);
        }

        log.info("Starting ProverService.proveOwn... : session [{}], user [{}], ticket [{}]",
                session.getId(), user.getId(), ticket.getId());
        ProverService.Proof proof = proverService.proveOwn(
                session.getId(),
                idx,
                user.getIcCommitment()
        );

        challenge.setUsed();

        String json;
        try {
            json = objectMapper.writeValueAsString(proof.getProof());
        } catch (Exception e) {
            log.error("Failed to serialize proof : session[{}], user[{}], ticket[{}]",
                    session.getId(), user.getId(), ticket.getId(), e);
            throw new CustomException(ErrorStatus.JSON_CONVERT_FAILED);
        }

        OwnProof ownProof = OwnProof.builder()
                .id(UUID.randomUUID().toString())
                .sessionId(session.getId())
                .proofJson(json)
                .root(proof.getPublicSignals().get(1))
                .nullifier(proof.getPublicSignals().get(2))
                .build();
        ownProofRepository.save(ownProof);

        log.info("Creating QR Code image... : session [{}], user [{}], ticket [{}]",
                session.getId(), user.getId(), ticket.getId());
        MultipartFile qrCode = qrCodeGenerator.generateQrCodeFile(ownProof.getId());

        return ProofQrCodeDTO.builder()
                .qrCodeUrl(s3UploadService.saveFile(qrCode))
                .identityType(user.getIdentityType())
                .build();
    }

}
