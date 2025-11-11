package com.example.demo.domain.concert.controller;

import com.example.demo.domain.concert.dto.response.BuyerConcertDetailDTO;
import com.example.demo.domain.concert.dto.response.PriceWeiDTO;
import com.example.demo.domain.concert.service.BuyerConcertService;
import com.example.demo.domain.concert.service.SessionService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@Slf4j
@RestController
@RequestMapping("/api/buyer/concerts")
@RequiredArgsConstructor
public class BuyerConcertController {

    private final BuyerConcertService buyerConcertService;
    private final SessionService sessionService;

    @Operation(summary = "구매자 - 공연 상세 조회")
    @GetMapping("/{concertId}")
    public ApiResponse<BuyerConcertDetailDTO> getConcertDetailForBuyer(@PathVariable Long concertId) {
        log.info("GET /api/buyer/concerts/{}", concertId);
        return ApiResponse.onSuccess(_OK, buyerConcertService.getConcertDetailForBuyer(concertId));
    }

    @Operation(summary = "티켓 가격 확인")
    @GetMapping("/{sessionId}/price")
    public ApiResponse<PriceWeiDTO> getPriceWei(@PathVariable("sessionId") Long sessionId) {
        log.info("GET /api/buyer/concerts/{}/price", sessionId);
        return ApiResponse.onSuccess(_OK, sessionService.getPriceWei(sessionId));
    }
}
