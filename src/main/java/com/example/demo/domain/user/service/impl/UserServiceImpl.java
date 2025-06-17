package com.example.demo.domain.user.service.impl;

import com.example.demo.global.security.dto.LoginResponseDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.security.JwtProvider;
import com.example.demo.global.security.dto.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == "anonymousUser") {
            throw new CustomException(ErrorStatus.USER_NOT_FOUND);
        }

        Long userId = (Long) authentication.getPrincipal();

        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public LoginResponseDTO loginWithWallet(String walletAddress) {
        if (walletAddress == null || walletAddress.isBlank()) {
            throw new CustomException(ErrorStatus.INVALID_INPUT);
        }

        if (!walletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
            throw new CustomException(ErrorStatus.INVALID_WALLET_ADDRESS);
        }

        User user = userRepository.findByWalletAddress(walletAddress).orElse(null);

        if (user == null) {
            user = User.builder()
                    .walletAddress(walletAddress)
                    .build();
            userRepository.save(user);
        }

        String token = jwtProvider.generateToken(user.getId());
        return LoginResponseDTO.builder()
                .token(token)
                .build();

    }

    @Override
    public UserInfoDTO getUserInfo() {
        return UserInfoDTO.builder()
                .id(getCurrentUser().getId())
                .build();
    }
}
