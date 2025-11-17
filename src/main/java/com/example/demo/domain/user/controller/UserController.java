package com.example.demo.domain.user.controller;

import com.example.demo.domain.metadata.dto.PhotoCardDTO;
import com.example.demo.domain.ticket.dto.response.TicketCardDTO;
import com.example.demo.domain.user.dto.response.PassportInfoDTO;
import com.example.demo.domain.user.dto.response.WalletDTO;
import com.example.demo.domain.user.service.MypageService;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.domain.user.dto.request.MetaMaskLoginDTO;
import com.example.demo.domain.user.dto.response.UserInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MypageService mypageService;

    @Operation(summary = "메타마스크 연결하기")
    @PostMapping("/signup/metamask/complete")
    public ApiResponse<?> connectMetaMaskWallet(
            @RequestBody MetaMaskLoginDTO request
    ) {
        log.info("REQ   POST /api/user/signup/metamask/complete");
        userService.connectWallet(request);
        log.info("RES   POST /api/user/signup/metamask/complete");

        return ApiResponse.onSuccess(_OK, null);
    }

    @Operation(summary = "현재 로그인한 사용자 확인하기")
    @GetMapping("/user-info")
    public ApiResponse<UserInfoDTO> getUserInfo() {
        return ApiResponse.onSuccess(_OK, userService.getUserInfo());
    }

    @Operation(summary = "여권 정보 확인하기")
    @GetMapping("/passport")
    public ApiResponse<PassportInfoDTO> getPassportInfo() {
        log.info("REQ   GET /api/user/passport");
        PassportInfoDTO response = userService.getPassportInfo();
        log.info("RES   GET /api/user/passport");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "지갑 정보 조회하기")
    @GetMapping("/wallet")
    public ApiResponse<WalletDTO> getWallet() {
        log.info("REQ   GET /api/user/wallet");
        WalletDTO response = mypageService.getWalletInfo();
        log.info("RES   GET /api/user/wallet");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "내 티켓 목록 조회하기")
    @GetMapping("/tickets")
    public ApiResponse<List<TicketCardDTO>> getTickets() {
        log.info("REQ   GET /api/user/tickets");
        List<TicketCardDTO> response = mypageService.getMyTickets();
        log.info("RES   GET /api/user/tickets");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "내 포토카드 목록 조회하기")
    @GetMapping("/photocards")
    public ApiResponse<List<PhotoCardDTO>> getPhotoCards() {
        log.info("REQ   GET /api/user/photocards");
        List<PhotoCardDTO> response = mypageService.getMyPhotoCards();
        log.info("RES   GET /api/user/photocards");
        return ApiResponse.onSuccess(_OK, response);
    }

}
