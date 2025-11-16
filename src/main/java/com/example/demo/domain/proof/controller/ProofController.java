package com.example.demo.domain.proof.controller;

import com.example.demo.domain.proof.dto.response.ChallengeDTO;
import com.example.demo.domain.proof.dto.response.ProofDTO;
import com.example.demo.domain.proof.dto.request.ProofAuthDTO;
import com.example.demo.domain.proof.dto.response.ProofQrCodeDTO;
import com.example.demo.domain.proof.service.ProofService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@Slf4j
@RestController
@RequestMapping("/api/proofs")
@RequiredArgsConstructor
public class ProofController {

    private final ProofService proofService;

    @Operation(summary = "당첨 증명 생성")
    @PostMapping("/win")
    public ApiResponse<ProofDTO> issueWinProof(
            @RequestBody ProofAuthDTO request
    ) {
        log.info("POST /api/proofs/win");
        return ApiResponse.onSuccess(_OK, proofService.issueWinProof(request));
    }

    @Operation(summary = "소유 증명 서명 챌린지 발급")
    @PostMapping("/own/challenge")
    public ApiResponse<ChallengeDTO> issueChallengeForOwn(
            @RequestParam Long ticketId
    ) {
        log.info("POST /api/proofs/own?ticketId={}", ticketId);
        return ApiResponse.onSuccess(_OK, proofService.issueChallenge(ticketId));
    }

    @Operation(summary = "소유 증명 생성")
    @PostMapping("/own")
    public ApiResponse<ProofQrCodeDTO> issueOwnProof(
            @RequestBody ProofAuthDTO request
    ) {
        log.info("POST /api/proofs/own");
        return ApiResponse.onSuccess(_OK, proofService.issueOwnProof(request));
    }

}
