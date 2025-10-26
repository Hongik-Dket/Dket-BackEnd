package com.example.demo.domain.resale.controller;

import com.example.demo.domain.resale.dto.request.ResaleListingDTO;
import com.example.demo.domain.resale.service.ResaleService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/resales")
@RequiredArgsConstructor
public class ResaleController {

    private final ResaleService resaleService;

    @Operation(summary = "리세일 티켓 판매")
    @PostMapping("/{ticketId}")
    public ApiResponse<?> listResale(
            @PathVariable("ticketId") Long ticketId,
            @RequestBody ResaleListingDTO request
    ) {
        resaleService.createResale(ticketId, request);

        return ApiResponse.onSuccess(_OK, null);
    }
}
