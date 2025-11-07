package com.example.demo.global.infra.blockchain.service.impl;

import com.example.demo.global.infra.blockchain.service.DketNFTViewService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.exceptions.ContractCallException;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketNFTViewServiceImpl implements DketNFTViewService {

    private final Web3j web3j;
    private final Credentials credentials;

    @Value("${web3.nft-contract-address}")
    private String contractAddress;

    private DketNFT dketNFT;

    @PostConstruct
    public void init() {
        dketNFT = DketNFT.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );
    }

    @Override
    public String getOwnerWallet(BigInteger tokenId) {
        try {
            return dketNFT.ownerOf(tokenId).send().trim().toLowerCase();
        } catch (ContractCallException e) {
            log.error("Token [{}] owner 호출 실패: {}", tokenId, e.getMessage(), e);
            throw new CustomException(ErrorStatus.TOKEN_INVALID);
        } catch (Exception e) {
            log.error("Token [{}] ownerOf 트랜잭션 실패: {}", tokenId, e.getMessage(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
        }
    }

    @Override
    public String getTokenUri(BigInteger tokenId) {
        try {
            return dketNFT.tokenURI(tokenId).send();
        } catch (ContractCallException e) {
            log.error("Token [{}] URI 호출 실패: {}", tokenId, e.getMessage(), e);
            throw new CustomException(ErrorStatus.TOKEN_INVALID);
        } catch (Exception e) {
            log.error("Token [{}] TokenUri 트랜잭션 실패: {}", tokenId, e.getMessage(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
        }
    }

    @Override
    public boolean isEntered(BigInteger tokenId) {
        try {
            BigInteger enterAt = dketNFT.enteredAt(tokenId).send();

            return enterAt != null && enterAt.signum() > 0;
        } catch (ContractCallException e) {
            log.error("Token [{}] enteredAt 호출 실패: {}", tokenId, e.getMessage(), e);
            throw new CustomException(ErrorStatus.TOKEN_INVALID);
        } catch (Exception e) {
            log.error("Token [{}] enteredAt 트랜잭션 실패: {}", tokenId, e.getMessage(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_ESTIMATE_GAS_FAILED);
        }
    }
}
