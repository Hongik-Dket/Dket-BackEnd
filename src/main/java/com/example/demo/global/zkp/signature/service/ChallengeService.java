package com.example.demo.global.zkp.signature.service;

import com.example.demo.global.zkp.signature.entity.Challenge;
import com.example.demo.global.zkp.signature.enums.ChallengePurpose;
import com.example.demo.global.zkp.signature.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static com.example.demo.global.base.Constants.CHALLENGE_EXPIRATION_MINUTES;
import static com.example.demo.global.util.Hexes.random32BytesHex;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    @Transactional
    public Challenge issueChallenge(Long userId, Long sessionId, ChallengePurpose purpose) {
        String nonce = random32BytesHex();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CHALLENGE_EXPIRATION_MINUTES);

        Challenge challenge = Challenge.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .purpose(purpose)
                .sessionId(sessionId)
                .nonceHex(nonce)
                .expiresAt(expiresAt)
                .build();

        challenge.setMessage(buildChallengeMessage(challenge));
        challengeRepository.save(challenge);

        return challenge;
    }

    private String buildChallengeMessage(Challenge c) {
        return String.format(
                "DKET|%s|user:%d|session:%d|nonce:%s|exp:%d",
                c.getPurpose(),
                c.getUserId(),
                c.getSessionId(),
                c.getNonceHex(),
                c.getExpiresAt().atZone(ZoneId.of("Asia/Seoul")).toEpochSecond()
        );
    }

    @Transactional
    public Challenge issueChallengeForResale(Long userId, Long resaleId, ChallengePurpose purpose) {
        String nonce = random32BytesHex();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CHALLENGE_EXPIRATION_MINUTES);

        Challenge challenge = Challenge.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .purpose(purpose)
                .resaleId(resaleId)
                .nonceHex(nonce)
                .expiresAt(expiresAt)
                .build();

        challenge.setMessage(buildChallengeMessageForResale(challenge));
        challengeRepository.save(challenge);

        return challenge;
    }

    private String buildChallengeMessageForResale(Challenge c) {
        return String.format(
                "DKET|%s|user:%d|resale:%d|nonce:%s|exp:%d",
                c.getPurpose(),
                c.getUserId(),
                c.getResaleId(),
                c.getNonceHex(),
                c.getExpiresAt().atZone(ZoneId.of("Asia/Seoul")).toEpochSecond()
        );
    }

}
