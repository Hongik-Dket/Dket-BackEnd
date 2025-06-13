package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.DTO.MetaMaskLoginRequestDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.code.ReasonDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "메타마스크로 로그인하기")
    @PostMapping("/metamask/complete")
    public ResponseEntity<ReasonDTO> completeMetaMaskLogin(
            @RequestBody MetaMaskLoginRequestDTO request) {

        User user = userService.loginWithWallet(request.getWalletAddress());

        return ResponseEntity.ok(
                ReasonDTO.builder()
                        .isSuccess(true)
                        .code("COMMON200")
                        .message("지갑 로그인 완료")
                        .build()
        );
    }
}
