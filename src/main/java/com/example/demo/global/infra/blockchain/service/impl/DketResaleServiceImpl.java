package com.example.demo.global.infra.blockchain.service.impl;

import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.global.infra.blockchain.contracts.DketResale;
import com.example.demo.global.infra.blockchain.service.DketResaleService;
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
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketResaleServiceImpl implements DketResaleService {

    private final Web3j web3j;
    private final Credentials credentials;

    @Value("${web3.resale-contract-address}")
    private String contractAddress;

    private DketResale dketResale;

    @PostConstruct
    public void init() {
        dketResale = DketResale.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );
    }

    @Override
    public String listResaleOnChain(Resale resale){
        try {
            var tx = dketResale.listResale(
                    BigInteger.valueOf(resale.getId()),
                    resale.getTicket().getTokenId(),
                    BigInteger.valueOf(resale.getSession().getId()),
                    resale.getSeller().getWalletAddress(),
                    resale.getPriceWei()
            ).send();
            log.info("send DketResale.listResale: resaleId={}", resale.getId());

            return tx.getTransactionHash();
        } catch (Exception e) {
            log.error("Resale [{}] 온체인 기록 실패", resale.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }

    @Override
    public void cancelResaleBatch(List<Long> resaleIds) {
        List<BigInteger> ids = resaleIds.stream()
                .map(BigInteger::valueOf)
                .toList();

        try {
            dketResale.cancelResaleBatch(ids).send();
            log.info("send DketResale.cancelResaleBatch: resaleIds={}", resaleIds);
        } catch (Exception e) {
            log.error("cancelResaleBatch 실패 : resaleIds = {}", resaleIds, e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
    }
}
