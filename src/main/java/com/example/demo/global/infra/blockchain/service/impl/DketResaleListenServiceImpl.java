package com.example.demo.global.infra.blockchain.service.impl;

import com.example.demo.domain.resale.service.ResaleService;
import com.example.demo.global.infra.blockchain.contracts.DketResale;
import com.example.demo.global.infra.blockchain.service.DketResaleListenService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tx.gas.DefaultGasProvider;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketResaleListenServiceImpl implements DketResaleListenService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final ResaleService resaleService;

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

        listenToResaleListed();
        listenToResaleSold();
    }

    private void listenToResaleListed() {
        dketResale.resaleListedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    Long resaleId = event.resaleId.longValue();
                    resaleService.completeResaleListing(resaleId);
                },
                error -> {
                    log.error("resaleListed 이벤트 수신 중 예외 발생", error);
                    }
                );
    }

    private void listenToResaleSold() {
        dketResale.resaleSoldEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(
                        event -> {
                            Long resaleId = event.resaleId.longValue();
                            resaleService.completeResalePurchase(resaleId);
                        },
                        error -> {
                            log.error("resaleSold 이벤트 수신 중 예외 발생", error);
                        }
                );
    }
}
