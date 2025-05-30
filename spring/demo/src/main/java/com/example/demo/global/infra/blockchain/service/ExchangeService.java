package com.example.demo.global.infra.blockchain.service;

import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String COINGECKO_API =
            "https://api.coingecko.com/api/v3/simple/price?ids=ethereum&vs_currencies=krw";
    private static final BigDecimal WEI_IN_ETH = new BigDecimal("1000000000000000000");

    private BigDecimal getEthPriceInKrw() {
        String response = restTemplate.getForObject(COINGECKO_API, String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("ethereum").path("krw").decimalValue();
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.BLOCKCHAIN_GET_ETH_PRICE_FAILED);
        }
    }

    // KRW → Wei
    public BigInteger convertKrwToWei(BigDecimal krwAmount) {
        BigDecimal ethPrice = getEthPriceInKrw();
        BigDecimal ethAmount = krwAmount.divide(ethPrice, 18, BigDecimal.ROUND_DOWN);

        return ethAmount.multiply(WEI_IN_ETH).toBigInteger();
    }

    // Wei → KRW
    public BigDecimal convertWeiToKrw(BigInteger weiAmount) {
        BigDecimal ethPrice = getEthPriceInKrw();
        BigDecimal ethAmount = new BigDecimal(weiAmount).divide(WEI_IN_ETH, 18, BigDecimal.ROUND_DOWN);

        return ethAmount.multiply(ethPrice);
    }
}
