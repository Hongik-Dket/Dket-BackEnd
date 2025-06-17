package com.example.demo.global.infra.blockchain.service;

import java.math.BigDecimal;

public interface WalletService {

    BigDecimal getEthBalance(String address);

}
