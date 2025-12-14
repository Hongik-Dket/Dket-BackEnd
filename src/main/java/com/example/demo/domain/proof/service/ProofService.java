package com.example.demo.domain.proof.service;

import com.example.demo.domain.proof.dto.response.ChallengeDTO;
import com.example.demo.domain.proof.dto.response.ProofDTO;
import com.example.demo.domain.proof.dto.request.ProofAuthDTO;
import com.example.demo.domain.proof.dto.response.ProofQrCodeDTO;

public interface ProofService {

    ProofDTO issueWinProof(ProofAuthDTO request);

    ChallengeDTO issueChallenge(Long ticketId);

    ProofQrCodeDTO getOwnProof(ProofAuthDTO request);

    void issueOwnProof(Long ticketId);

}
