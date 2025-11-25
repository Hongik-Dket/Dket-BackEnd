package com.example.demo.global.config;

import com.example.demo.global.infra.blockchain.contracts.DketNFT;
import com.example.demo.global.infra.blockchain.contracts.DketResale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

@Configuration
public class ContractConfig {

    @Value("${web3.nft-contract-address}")
    private String nftContractAddress;

    @Value("${web3.resale-contract-address}")
    private String resaleContractAddress;

    @Bean
    public DketNFT dketNFT(Web3j web3j,
                           RawTransactionManager txManager) {
        return DketNFT.load(
                nftContractAddress,
                web3j,
                txManager,
                new DefaultGasProvider()
        );
    }

    @Bean
    public DketResale dketResale(Web3j web3j,
                                 RawTransactionManager txManager) {
        return DketResale.load(
                resaleContractAddress,
                web3j,
                txManager,
                new DefaultGasProvider()
        );
    }
}