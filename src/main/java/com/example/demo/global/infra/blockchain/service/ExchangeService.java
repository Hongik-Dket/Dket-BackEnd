package com.example.demo.global.infra.blockchain.service;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface ExchangeService {

    BigInteger convertKrwToWei(BigDecimal krwAmount);

    BigDecimal convertWeiToKrw(BigInteger weiAmount);
}
