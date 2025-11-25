package com.example.demo.global.infra.blockchain.service.impl;

import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.global.infra.blockchain.contracts.DketResale;
import com.example.demo.global.infra.blockchain.service.DketResaleService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DketResaleServiceImpl implements DketResaleService {

    private final DketResale dketResale;

    @Override
    public void listResaleOnChain(Resale resale){
        log.info("Sending DketResale.listResale: resaleId={}", resale.getId());
        try {
            dketResale.listResale(
                    BigInteger.valueOf(resale.getId()),
                    resale.getTicket().getTokenId(),
                    BigInteger.valueOf(resale.getSession().getId()),
                    resale.getSeller().getWalletAddress(),
                    resale.getPriceWei()
            ).send();
        } catch (Exception e) {
            log.error("Resale [{}] 온체인 기록 실패", resale.getId(), e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketResale.listResale: resaleId={}", resale.getId());
    }

    @Override
    public void cancelResaleBatch(List<Long> resaleIds) {
        List<BigInteger> ids = resaleIds.stream()
                .map(BigInteger::valueOf)
                .toList();

        log.info("Sending DketResale.cancelResaleBatch: resaleIds={}", resaleIds);
        try {
            dketResale.cancelResaleBatch(ids).send();
        } catch (Exception e) {
            log.error("cancelResaleBatch 실패 : resaleIds = {}", resaleIds, e);
            throw new CustomException(ErrorStatus.BLOCKCHAIN_TRANSACTION_FAILED);
        }
        log.info("Completed DketResale.cancelResaleBatch: resaleIds={}", resaleIds);
    }
}
