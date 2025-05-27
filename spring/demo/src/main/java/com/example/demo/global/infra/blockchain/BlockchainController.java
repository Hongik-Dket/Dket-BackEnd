package com.example.demo.global.infra.blockchain;

import com.example.demo.domain.event.service.SessionService;
import com.example.demo.global.infra.blockchain.dto.SessionOnDrawnRequestDTO;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/blockchain/session-drawn")
@RequiredArgsConstructor
public class BlockchainController {

    private final SessionService sessionService;

    @Operation(summary = "온체인 Session 추첨 완료")
    @PostMapping("")
    public ApiResponse<?> onSessionDrawn(@RequestBody SessionOnDrawnRequestDTO request) {
        sessionService.saveWinners(request);

        return ApiResponse.onSuccess(_OK, null);
    }
}
