package com.example.demo.domain.user.service.impl;

import com.example.demo.domain.user.dto.response.PassportInfoDTO;
import com.example.demo.domain.user.entity.PassportInfo;
import com.example.demo.domain.user.enums.IdentityType;
import com.example.demo.domain.user.repository.PassportInfoRepository;
import com.example.demo.domain.user.dto.request.MetaMaskLoginDTO;
import com.example.demo.domain.user.dto.response.UserInfoDTO;
import com.example.demo.global.security.dto.response.LoginResponseDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.security.JwtProvider;
import com.example.demo.global.security.dto.request.PassportSignupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.example.demo.domain.user.converter.UserConverter.toPassportInfo;
import static com.example.demo.domain.user.converter.UserConverter.toPassportInfoDTO;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PassportInfoRepository passportInfoRepository;

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
    public LoginResponseDTO signupWithPassport(PassportSignupDTO request) {
        if (passportInfoRepository.existsByPassportNumber(request.getPassportNumber())) {
            throw new CustomException(ErrorStatus.USER_ALREADY_EXISTS);
        }

        if (!request.getPassportExpiry().isAfter(LocalDate.now())) {
            throw new CustomException(ErrorStatus.USER_INVALID_PASSPORT);
        }

        String name = request.getFirstName() + " " +  request.getLastName();

        User user = User.builder()
                .name(name)
                .birth(request.getBirthDate())
                .identityType(IdentityType.PASSPORT)
                .build();

        userRepository.save(user);

        PassportInfo passportInfo = toPassportInfo(user, request);
        passportInfoRepository.save(passportInfo);

        String token = jwtProvider.generateToken(user.getId());
        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }

    @Override
    @Transactional
    public void loginWithWallet(MetaMaskLoginDTO request) {
        String walletAddress = request.getWalletAddress();

        if (walletAddress == null || walletAddress.isBlank()) {
            throw new CustomException(ErrorStatus.USER_INVALID_INPUT);
        }

        if (!walletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
            throw new CustomException(ErrorStatus.USER_INVALID_WALLET);
        }

        walletAddress = walletAddress.toLowerCase();
        if (userRepository.existsByWalletAddress(walletAddress)) {
            throw new CustomException(ErrorStatus.USER_WALLET_ALREADY_REGISTERED);
        }

        getCurrentUser().setWalletAddress(walletAddress);
    }

    @Override
    public PassportInfoDTO getPassportInfo(){
        User user = getCurrentUser();

        if (user.getIdentityType() != IdentityType.PASSPORT) {
            throw new CustomException(ErrorStatus.USER_NOT_REGISTERED_WITH_PASSPORT);
        }

        PassportInfo passport = passportInfoRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.PASSPORT_INFO_NOT_FOUND));

        return toPassportInfoDTO(passport);
    }

    @Override
    public UserInfoDTO getUserInfo() {
        return UserInfoDTO.builder()
                .id(getCurrentUser().getId())
                .build();
    }

    @Override
    public LoginResponseDTO refreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        String token = jwtProvider.refreshToken(user.getId());
        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }
}
