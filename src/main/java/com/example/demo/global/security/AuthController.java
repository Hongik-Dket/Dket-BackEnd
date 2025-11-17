package com.example.demo.global.security;

import com.example.demo.domain.user.dto.request.MetaMaskLoginDTO;
import com.example.demo.global.security.dto.response.LoginResponseDTO;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.global.security.dto.request.PassportSignupDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "여권 기반 회원가입")
    @PostMapping("/signup/passport")
    public ApiResponse<LoginResponseDTO> signupWithPassport(
            @RequestBody PassportSignupDTO request
            ) {
        log.info("REQ   POST /auth/signup/passport");
        LoginResponseDTO response = userService.signupWithPassport(request);
        log.info("RES   POST /auth/signup/passport");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "메타마스크로 로그인")
    @PostMapping("/login/metamask")
    public ApiResponse<LoginResponseDTO> loginWithMetaMask(
            @RequestBody MetaMaskLoginDTO request
    ) {
        log.info("REQ   POST /auth/metamask");
        LoginResponseDTO response = userService.loginWithMetaMask(request);
        log.info("RES   POST /auth/metamask");
        return ApiResponse.onSuccess(_OK, response);
    }

    @Operation(summary = "개발용 리프레쉬 토큰 발급")
    @GetMapping("/refresh")
    public ApiResponse<LoginResponseDTO> refreshToken(
            @RequestParam("userId") Long userId
    ) {
        return ApiResponse.onSuccess(_OK, userService.refreshToken(userId));
    }
}
