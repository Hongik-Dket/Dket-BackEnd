package com.example.demo.global.infra.blockchain.service.impl;

import com.example.demo.global.infra.blockchain.service.WalletService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final Web3j web3j;

    @Override
    public BigDecimal getEthBalance(String address) {
        try {
            if (!isValidAddress(address)) {
                throw new CustomException(ErrorStatus.WALLET_INVALID_ADDRESS);
            }

            BigInteger balanceInWei = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();

            return Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER);

        } catch (Exception e) {
            log.error("Wallet [{}] 잔액 조회 실패: {}", address, e.getMessage(), e);
            throw new CustomException(ErrorStatus.WALLET_GET_BALANCE_FAILED);
        }
    }

    private boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[0-9a-fA-F]{40}$");
    }
}

