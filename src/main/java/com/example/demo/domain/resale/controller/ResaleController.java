package com.example.demo.domain.resale.controller;

import com.beust.ah.A;
import com.example.demo.domain.resale.dto.request.ResaleListingDTO;
import com.example.demo.domain.resale.dto.response.ResaleAuthDTO;
import com.example.demo.domain.resale.dto.response.ResaleCardDTO;
import com.example.demo.domain.resale.dto.response.ResaleDetailDTO;
import com.example.demo.domain.resale.dto.response.ResaleInfoDTO;
import com.example.demo.domain.resale.service.ResaleService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/resales")
@RequiredArgsConstructor
public class ResaleController {

    private final ResaleService resaleService;

    @Operation(summary = "리세일 티켓 판매")
    @PostMapping("/{ticketId}")
    public ApiResponse<ResaleInfoDTO> listResale(
            @PathVariable("ticketId") Long ticketId,
            @RequestBody ResaleListingDTO request
    ) {
        return ApiResponse.onSuccess(_OK, resaleService.createResale(ticketId, request));
    }

    @Operation(summary = "특정 세션 리세일 티켓 조회")
    @GetMapping("")
    public ApiResponse<List<ResaleCardDTO>> getSessionResales(
            @RequestParam("sessionId") Long sessionId
    ) {
        return ApiResponse.onSuccess(_OK, resaleService.getSessionResales(sessionId));
    }

    @Operation(summary = "리세일 예약")
    @PatchMapping("/{resaleId}/reserve")
    public ApiResponse<ResaleDetailDTO> reserveResale(
            @PathVariable("resaleId") Long resaleId
    ) {
        return ApiResponse.onSuccess(_OK, resaleService.reserveResale(resaleId));
    }

    @Operation(summary = "리세일 예약 취소")
    @DeleteMapping("/{resaleId}/reserve")
    public ApiResponse<?> cancelResaleReservation(
            @PathVariable("resaleId") Long resaleId
    ) {
        resaleService.cancelResaleReservation(resaleId);

        return ApiResponse.onSuccess(_OK, null);
    }

    @Operation(summary = "리세일 구매 서명 요청")
    @PostMapping("/{resaleId}/purchase")
    public ApiResponse<ResaleAuthDTO> authorizeResalePurchase(
            @PathVariable("resaleId") Long resaleId
    ) {
      return ApiResponse.onSuccess(_OK, resaleService.authorizeResalePurchase(resaleId));
    }
}
