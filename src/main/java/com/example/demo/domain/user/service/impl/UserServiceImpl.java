package com.example.demo.domain.user.service.impl;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        // ToDO: 로그인 구현
        return userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));
    }

    @Override
    public User loginWithWallet(String walletAddress) {
        if (walletAddress == null || walletAddress.isBlank()) {
            throw new CustomException(ErrorStatus.INVALID_INPUT);
        }
        if (!walletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
            throw new CustomException(ErrorStatus.INVALID_WALLET_ADDRESS);
        }
        return userRepository.findByWalletAddress(walletAddress)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setWalletAddress(walletAddress);
                    return userRepository.save(newUser);
                });
    }
}
