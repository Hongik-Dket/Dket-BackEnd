package com.example.demo.global.infra.blockchain.service;

import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final Web3j web3j;

    public BigDecimal getEthBalance(String address) {
        try {
            if (!isValidAddress(address))
                throw new CustomException(ErrorStatus.WALLET_INVALID_ADDRESS);

            BigInteger balanceInWei = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();

            return Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER);

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorStatus.WALLET_GET_BALANCE_FAILED);
        }
    }

    private boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[0-9a-fA-F]{40}$");
    }
}
