package com.example.demo.domain.main.controller;

import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.EventCardListDTO;
import com.example.demo.domain.main.service.BuyerHomeService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/buyer/home")
@RequiredArgsConstructor
public class BuyerHomeController {

    private final BuyerHomeService buyerHomeService;

    @Operation(summary = "구매자 - 홈 화면 조회")
    @GetMapping("")
    public ApiResponse<BuyerHomeResponseDTO> getBuyerHome() {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getHomeForBuyer());
    }

    @Operation(summary = "구매자 - 인기 공연 조회")
    @GetMapping("/popular")
    public ApiResponse<EventCardListDTO> getPopularEvents() {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getPopularEvents());
    }

    @Operation(summary = "구매자 - 응모한 공연 조회")
    @GetMapping("/applied")
    public ApiResponse<EventCardListDTO> getAppliedEvents() {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getAppliedEvents());
    }

    @Operation(summary = "구매자 - 구매한 공연 조회")
    @GetMapping("/purchased")
    public ApiResponse<EventCardListDTO> getPurchasedEvents() {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getPurchasedEvents());
    }
}