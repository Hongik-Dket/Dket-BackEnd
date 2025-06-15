package com.example.demo.domain.user.service;

import com.example.demo.domain.user.dto.response.WalletDTO;
import com.example.demo.domain.user.entity.User;

public interface UserService {

    User getCurrentUser();

    User loginWithWallet(String walletAddress);

    WalletDTO getWalletInfo();

}
