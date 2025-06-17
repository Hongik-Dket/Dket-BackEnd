package com.example.demo.domain.user.controller;

import com.example.demo.domain.metadata.dto.PhotoCardDTO;
import com.example.demo.domain.metadata.dto.PhotoCardDetailDTO;
import com.example.demo.domain.ticket.dto.TicketDTO;
import com.example.demo.domain.user.dto.request.MetaMaskLoginRequestDTO;
import com.example.demo.domain.user.dto.response.WalletDTO;
import com.example.demo.domain.user.service.MypageService;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.global.response.code.ReasonDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MypageService mypageService;

    @Operation(summary = "메타마스크로 로그인하기")
    @PostMapping("/login/metamask/complete")
    public ResponseEntity<ReasonDTO> completeMetaMaskLogin(
            @RequestBody MetaMaskLoginRequestDTO request) {

        userService.loginWithWallet(request.getWalletAddress());

        return ResponseEntity.ok(
                ReasonDTO.builder()
                        .isSuccess(true)
                        .code("COMMON200")
                        .message("지갑 로그인 완료")
                        .build()
        );
    }

    @Operation(summary = "지갑 정보 조회하기")
    @GetMapping("/wallet")
    public ApiResponse<WalletDTO> getWallet() {
        return ApiResponse.onSuccess(_OK, mypageService.getWalletInfo());
    }

    @Operation(summary = "내 티켓 목록 조회하기")
    @GetMapping("/tickets")
    public ApiResponse<List<TicketDTO>> getTickets() {
        return ApiResponse.onSuccess(_OK, mypageService.getMyTickets());
    }

    @Operation(summary = "내 포토카드 목록 조회하기")
    @GetMapping("/photocards")
    public ApiResponse<List<PhotoCardDTO>> getPhotoCards() {
        return ApiResponse.onSuccess(_OK, mypageService.getMyPhotoCards());
    }

    @Operation(summary = "포토카드 상세 조회하기")
    @GetMapping("/photocards/{ticketId}")
    public ApiResponse<PhotoCardDetailDTO> getPhotoCard(@PathVariable Long ticketId) {
        return ApiResponse.onSuccess(_OK, mypageService.getMyPhotoCardDetail(ticketId));
    }
}
