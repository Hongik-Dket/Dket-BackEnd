package com.example.demo.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
@RequiredArgsConstructor
public class Web3jConfig {

    @Value("${web3.network-url}")
    private String networkUrl;

    @Value("${web3.private-key}")
    private String privateKey;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(networkUrl));
    }

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }
}
