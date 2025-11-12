package com.example.demo.domain.proof.service;

import com.example.demo.domain.proof.dto.response.ProofDTO;
import com.example.demo.domain.proof.dto.request.WinProofAuthDTO;

public interface ProofService {

    ProofDTO issueWinProof(WinProofAuthDTO request);

}
