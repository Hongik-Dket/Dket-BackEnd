package com.example.demo.domain.event.controller;

import com.example.demo.domain.event.dto.response.BuyerEventDetailDTO;
import com.example.demo.domain.event.dto.response.PriceWeiDTO;
import com.example.demo.domain.event.service.BuyerEventService;
import com.example.demo.domain.event.service.SessionService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/buyer/events")
@RequiredArgsConstructor
public class BuyerEventController {

    private final BuyerEventService buyerEventService;
    private final SessionService sessionService;

    @Operation(summary = "구매자 - 공연 상세 조회")
    @GetMapping("/{eventId}")
    public ApiResponse<BuyerEventDetailDTO> getEventDetailForBuyer(@PathVariable Long eventId) {
        return ApiResponse.onSuccess(_OK, buyerEventService.getEventDetailForBuyer(eventId));
    }

    @Operation(summary = "티켓 가격 확인")
    @GetMapping("/{sessionId}/price")
    public ApiResponse<PriceWeiDTO> getPriceWei(@PathVariable("sessionId") Long sessionId) {
        return ApiResponse.onSuccess(_OK, sessionService.getPriceWei(sessionId));
    }
}
