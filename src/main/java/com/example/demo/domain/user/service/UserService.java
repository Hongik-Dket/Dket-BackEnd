package com.example.demo.domain.user.service;

import com.example.demo.global.security.dto.LoginResponseDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.dto.UserInfoDTO;

public interface UserService {

    User getCurrentUser();

    LoginResponseDTO loginWithWallet(String walletAddress);

    UserInfoDTO getUserInfo();

}
