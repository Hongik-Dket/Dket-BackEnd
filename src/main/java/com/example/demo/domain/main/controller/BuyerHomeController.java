package com.example.demo.domain.main.controller;

import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.ConcertCardListDTO;
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
    public ApiResponse<ConcertCardListDTO> getPopularConcerts() {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getPopularConcertsForBuyer());
    }

    @Operation(summary = "구매자 - 응모한 공연 조회")
    @GetMapping("/applied")
    public ApiResponse<ConcertCardListDTO> getAppliedConcerts() {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getAppliedConcertsForBuyer());
    }

    @Operation(summary = "구매자 - 구매한 공연 조회")
    @GetMapping("/purchased")
    public ApiResponse<ConcertCardListDTO> getPurchasedConcerts() {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getPurchasedConcertsForBuyer());
    }

    @Operation(summary = "구매자 - 전체 공연 조회")
    @GetMapping("/entire")
    public ApiResponse<ConcertCardListDTO> getEntireConcerts() {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getEntireConcertsForBuyer());
    }
}