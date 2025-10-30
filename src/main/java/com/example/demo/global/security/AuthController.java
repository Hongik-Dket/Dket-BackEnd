package com.example.demo.global.security;

import com.example.demo.global.security.dto.response.LoginResponseDTO;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.global.security.dto.request.PassportSignupDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

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
        return ApiResponse.onSuccess(_OK, userService.signupWithPassport(request));
    }
}
