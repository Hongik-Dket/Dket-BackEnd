package com.example.demo.global.infra.blockchain.service;

import com.example.demo.domain.ownership.service.OwnershipService;
import com.example.demo.domain.resale.service.ResaleService;
import com.example.demo.global.infra.blockchain.contracts.DketResale;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.DefaultBlockParameterName;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketResaleListenService {

    private final ResaleService resaleService;
    private final OwnershipService ownershipService;

    private final DketResale dketResale;

    @PostConstruct
    public void init() {

        listenToResaleListed();
        listenToResaleSold();
    }

    private void listenToResaleListed() {
        dketResale.resaleListedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    Long resaleId = event.resaleId.longValue();

                    log.info("resaleListed: resale [{}]", resaleId);

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

                            log.info("resaleSold: resale [{}]", resaleId);

                            resaleService.completeResalePurchase(resaleId);

                            String txHash = event.log.getTransactionHash();
                            long blockNumber = event.log.getBlockNumber().longValue();
                            int logIndex = event.log.getLogIndex().intValue();

                            ownershipService.transferOwnership(resaleId, txHash, blockNumber, logIndex);
                        },
                        error -> {
                            log.error("resaleSold 이벤트 수신 중 예외 발생", error);
                        }
                );
    }
}
