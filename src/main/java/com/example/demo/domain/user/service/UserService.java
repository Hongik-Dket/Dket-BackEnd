package com.example.demo.domain.user.service;

import com.example.demo.domain.user.dto.request.MetaMaskLoginDTO;
import com.example.demo.domain.user.dto.response.UserInfoDTO;
import com.example.demo.global.security.dto.response.LoginResponseDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.dto.request.PassportSignupDTO;

public interface UserService {

    User getCurrentUser();

    LoginResponseDTO signupWithPassport(PassportSignupDTO request);

    void loginWithWallet(MetaMaskLoginDTO request);

    UserInfoDTO getUserInfo();

    LoginResponseDTO refreshToken(Long userId);

}
