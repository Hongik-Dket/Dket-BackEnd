package com.example.demo.global.security;

import com.example.demo.global.security.dto.MetaMaskLoginRequestDTO;
import com.example.demo.global.security.dto.LoginResponseDTO;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.global.security.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @Operation(summary = "메타마스크로 로그인하기")
    @PostMapping("/login/metamask/complete")
    public ApiResponse<LoginResponseDTO> completeMetaMaskLogin(
            @RequestBody MetaMaskLoginRequestDTO request) {
        return ApiResponse.onSuccess(_OK, userService.loginWithWallet(request.getWalletAddress()));
    }

    @Operation(summary = "현재 로그인한 사용자 확인하기")
    @GetMapping("/user-info")
    public ApiResponse<UserInfoDTO> getUserInfo() {
        return ApiResponse.onSuccess(_OK, userService.getUserInfo());
    }
}
