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
        log.info("REQ   POST /api/proofs/win");
        ProofDTO response = proofService.issueWinProof(request);
        log.info("RES POST /api/proofs/win");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "소유 증명 서명 챌린지 발급")
    @GetMapping("/own/challenge")
    public ApiResponse<ChallengeDTO> issueChallengeForOwn(
            @RequestParam Long ticketId
    ) {
        log.info("REQ   GET /api/proofs/own?ticketId={}", ticketId);
        ChallengeDTO response = proofService.issueChallenge(ticketId);
        log.info("RES GET /api/proofs/own?ticketId={}", ticketId);
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "소유 증명 생성")
    @PostMapping("/own")
    public ApiResponse<ProofQrCodeDTO> issueOwnProof(
            @RequestBody ProofAuthDTO request
    ) {
        log.info("REQ   POST /api/proofs/own");
        ProofQrCodeDTO response = proofService.getOwnProof(request);
        log.info("RES   POST /api/proofs/own");
        return ApiResponse.onSuccess(_OK, response);
    }

}
