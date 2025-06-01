package com.example.demo.domain.main.controller;

import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.EventCardListDTO;
import com.example.demo.domain.main.service.BuyerHomeService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ApiResponse<EventCardListDTO> getPopularEvents(
            @PageableDefault(size = 20, sort = "applyEnd", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getPopularEvents(pageable));
    }

    @Operation(summary = "구매자 - 응모한 공연 조회")
    @GetMapping("/applied")
    public ApiResponse<EventCardListDTO> getAppliedEvents(
            @PageableDefault(size = 20, sort = "applyEnd", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getAppliedEvents(pageable));
    }

    @Operation(summary = "구매자 - 구매한 공연 조회")
    @GetMapping("/purchased")
    public ApiResponse<EventCardListDTO> getPurchasedEvents(
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getPurchasedEvents(pageable));
    }

    @Operation(summary = "구매자 - 전체 공연 조회")
    @GetMapping("/all")
    public ApiResponse<EventCardListDTO> getAllEvents(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.onSuccess(_OK, buyerHomeService.getEntireEvents(pageable));
    }
}