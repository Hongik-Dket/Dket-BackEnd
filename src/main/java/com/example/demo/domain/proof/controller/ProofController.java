package com.example.demo.domain.proof.controller;

import com.example.demo.domain.proof.dto.response.ProofDTO;
import com.example.demo.domain.proof.dto.request.WinProofAuthDTO;
import com.example.demo.domain.proof.service.ProofService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/proofs")
@RequiredArgsConstructor
public class ProofController {

    private final ProofService proofService;

    @Operation(summary = "당첨 증명 생성")
    @PostMapping("/win")
    public ApiResponse<ProofDTO> issueWinProof(
            @RequestBody WinProofAuthDTO request
    ) {
        return ApiResponse.onSuccess(_OK, proofService.issueWinProof(request));
    }

}
