package com.example.demo.domain.user.service.impl;

import com.example.demo.domain.user.dto.response.PassportInfoDTO;
import com.example.demo.domain.user.entity.PassportIdentity;
import com.example.demo.domain.user.enums.IdentityType;
import com.example.demo.domain.user.repository.PassportIdentityRepository;
import com.example.demo.domain.user.dto.request.MetaMaskLoginDTO;
import com.example.demo.global.security.dto.response.LoginResponseDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.security.JwtProvider;
import com.example.demo.global.security.dto.request.PassportSignupDTO;
import com.example.demo.global.zkp.ic.IcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.example.demo.domain.user.converter.UserConverter.toPassportInfo;
import static com.example.demo.domain.user.converter.UserConverter.toPassportInfoDTO;
import static com.example.demo.global.util.StringUtil.normalize;
import static com.example.demo.global.zkp.ic.IcService.newSalt16;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PassportIdentityRepository passportIdentityRepository;
    private final IcService icService;

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
        if (passportIdentityRepository.existsByPassportNumber(request.getPassportNumber())) {
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
        log.info("INSERT   userId={}", user.getId());

        PassportIdentity passportIdentity = toPassportInfo(user, request);
        passportIdentityRepository.save(passportIdentity);
        log.info("INSERT   passportIdentity={}", passportIdentity);

        String token = jwtProvider.generateToken(user.getId());
        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }

    @Override
    @Transactional
    public void connectWallet(MetaMaskLoginDTO request) {
        String walletAddress = request.getWalletAddress();
        String publicKey = request.getPublicKey();

        if (walletAddress == null || walletAddress.isBlank()
                || !walletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
            throw new CustomException(ErrorStatus.WALLET_INVALID_ADDRESS);
        }

        walletAddress = normalize(walletAddress);
        if (userRepository.existsByWalletAddress(walletAddress)) {
            throw new CustomException(ErrorStatus.WALLET_ALREADY_REGISTERED);
        }

        if (userRepository.existsByPublicKey(publicKey)) {
            throw new CustomException(ErrorStatus.SIG_ALREADY_REGISTERED_PUBKEY);
        }

        User user = getCurrentUser();

        if (user.getWalletAddress() != null) {
            throw new CustomException(ErrorStatus.USER_WALLET_ALREADY_REGISTERED);
        }
        if (user.getPublicKey() != null) {
            throw new CustomException(ErrorStatus.USER_PUBKEY_ALREADY_REGISTERED);
        }

        user.completeSignup(walletAddress, publicKey);
        log.info("UPDATE   user [{}] : walletAddress={}, publicKey={}", user.getId(), walletAddress, publicKey);

        LocalDate birth = user.getBirth();
        byte[] salt16 = newSalt16();
        IcService.IcCommitment ic = null;

        if (user.getIdentityType().equals(IdentityType.PASSPORT)) {
            PassportIdentity identity = passportIdentityRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new CustomException(ErrorStatus.PASSPORT_IDENTITY_NOT_FOUND));

            ic = icService.createFromPassport(identity, birth, salt16, walletAddress);

        } else if (user.getIdentityType().equals(IdentityType.PASS)) {

            // Todo: PASS 구현 후 ic 생성

        } else {
            throw new CustomException(ErrorStatus.USER_INVALID_SIGNUP);
        }

        user.createIc(ic);
        log.info("UPDATE   user [{}] IC", user.getId());
    }

    @Override
    public LoginResponseDTO loginWithMetaMask(MetaMaskLoginDTO request) {
        String walletAddress = request.getWalletAddress();

        if (walletAddress == null || walletAddress.isBlank()
                || !walletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
            throw new CustomException(ErrorStatus.WALLET_INVALID_ADDRESS);
        }

        walletAddress = normalize(walletAddress);
        User user = userRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        String token = jwtProvider.refreshToken(user.getId());
        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }

    @Override
    public PassportInfoDTO getPassportInfo(){
        User user = getCurrentUser();

        if (user.getIdentityType() != IdentityType.PASSPORT) {
            throw new CustomException(ErrorStatus.USER_NOT_REGISTERED_WITH_PASSPORT);
        }

        PassportIdentity passport = passportIdentityRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.PASSPORT_IDENTITY_NOT_FOUND));

        return toPassportInfoDTO(passport);
    }

}
