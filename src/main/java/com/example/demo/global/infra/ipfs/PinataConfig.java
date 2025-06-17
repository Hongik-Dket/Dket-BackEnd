package com.example.demo.global.infra.ipfs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pinata")
public class PinataConfig {
    private String apiKey;
    private String apiSecret;
    private String uploadUrl;
    private String gateway;
}
