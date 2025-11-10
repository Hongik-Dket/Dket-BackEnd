package com.example.demo.global.config;

import com.example.demo.global.infra.blockchain.ResaleSigner;
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

    @Value("${web3.rpc-wss-url}")
    private String wssUrl;

    @Value("${web3.network-url}")
    private String networkUrl;

    @Value("${web3.private-key}")
    private String privateKey;

    @Value("${web3.chain-id}")
    private long chainId;

    @Value("${web3.resale-contract-address}")
    private String verifyingContract;          // DketResale

    @Bean(destroyMethod = "shutdown")
    public Web3j web3j() throws Exception {
        if (wssUrl != null && !wssUrl.isBlank()) {
            var ws = new org.web3j.protocol.websocket.WebSocketService(wssUrl, true);
            ws.connect();
            return org.web3j.protocol.Web3j.build(ws);
        }

        okhttp3.OkHttpClient ok = new okhttp3.OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();
        return org.web3j.protocol.Web3j.build(new HttpService(networkUrl, ok, false));
    }

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }

    @Bean
    public ResaleSigner resaleSigner() {
        return new ResaleSigner(credentials(), chainId, verifyingContract);
    }
}
