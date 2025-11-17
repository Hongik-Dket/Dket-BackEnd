package com.example.demo.domain.main.controller;

import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.ConcertCardDTO;
import com.example.demo.domain.main.dto.ConcertCardListDTO;
import com.example.demo.domain.main.service.BuyerHomeService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@Slf4j
@RestController
@RequestMapping("/api/buyer/home")
@RequiredArgsConstructor
public class BuyerHomeController {

    private final BuyerHomeService buyerHomeService;

    @Operation(summary = "구매자 - 홈 화면 조회")
    @GetMapping("")
    public ApiResponse<BuyerHomeResponseDTO> getBuyerHome() {
        log.info("REQ   GET /api/buyer/home");
        BuyerHomeResponseDTO response = buyerHomeService.getHomeForBuyer();
        log.info("RES GET  /api/buyer/home");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "구매자 - 인기 공연 조회")
    @GetMapping("/popular")
    public ApiResponse<ConcertCardListDTO> getPopularConcerts() {
        log.info("REQ   GET /api/buyer/home/popular");
        ConcertCardListDTO response = buyerHomeService.getPopularConcertsForBuyer();
        log.info("RES   GET /api/buyer/home/popular");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "구매자 - 응모한 공연 조회")
    @GetMapping("/applied")
    public ApiResponse<ConcertCardListDTO> getAppliedConcerts() {
        log.info("REQ   GET /api/buyer/home/applied");
        ConcertCardListDTO response = buyerHomeService.getAppliedConcertsForBuyer();
        log.info("RES   GET /api/buyer/home/applied");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "구매자 - 구매한 공연 조회")
    @GetMapping("/purchased")
    public ApiResponse<ConcertCardListDTO> getPurchasedConcerts() {
        log.info("REQ   GET /api/buyer/home/purchased");
        ConcertCardListDTO response = buyerHomeService.getPurchasedConcertsForBuyer();
        log.info("RES   GET /api/buyer/home/purchased");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "구매자 - 전체 공연 조회")
    @GetMapping("/entire")
    public ApiResponse<ConcertCardListDTO> getEntireConcerts() {
        log.info("REQ   GET /api/buyer/home/entire");
        ConcertCardListDTO response = buyerHomeService.getEntireConcertsForBuyer();
        log.info("RES   GET /api/buyer/home/entire");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "구매자 - 검색")
    @GetMapping("/search")
    public ApiResponse<List<ConcertCardDTO>> searchConcerts(
            @RequestParam("keyword") String keyword
    ) {
        log.info("REQ   GET /api/buyer/home/search?keyword={}", keyword);
        List<ConcertCardDTO> response = buyerHomeService.searchConcert(keyword);
        log.info("RES   GET /api/buyer/home/search?keyword={}", keyword);
        return ApiResponse.onSuccess(_OK, response);
    }
}