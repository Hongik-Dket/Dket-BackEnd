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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.global.response.status.SuccessStatus._OK;

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
        userService.connectWallet(request);

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
        return ApiResponse.onSuccess(_OK, userService.getPassportInfo());
    }

    @Operation(summary = "지갑 정보 조회하기")
    @GetMapping("/wallet")
    public ApiResponse<WalletDTO> getWallet() {
        return ApiResponse.onSuccess(_OK, mypageService.getWalletInfo());
    }

    @Operation(summary = "내 티켓 목록 조회하기")
    @GetMapping("/tickets")
    public ApiResponse<List<TicketCardDTO>> getTickets() {
        return ApiResponse.onSuccess(_OK, mypageService.getMyTickets());
    }

    @Operation(summary = "내 포토카드 목록 조회하기")
    @GetMapping("/photocards")
    public ApiResponse<List<PhotoCardDTO>> getPhotoCards() {
        return ApiResponse.onSuccess(_OK, mypageService.getMyPhotoCards());
    }

}
