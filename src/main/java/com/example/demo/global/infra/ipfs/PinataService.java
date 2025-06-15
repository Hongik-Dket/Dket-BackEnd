package com.example.demo.global.infra.ipfs;

import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PinataService {

    private final PinataConfig pinataConfig;

    private static final String IPFS_HASH_KEY = "IpfsHash";

    public String uploadPhotoCard(MultipartFile file) {
        try {
            return uploadToPinata(file.getInputStream(), ContentType.DEFAULT_BINARY, file.getOriginalFilename());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.IPFS_UPLOAD_FAILED);
        }
    }

    @Async("pinataUploadExecutor")
    public CompletableFuture<String> uploadJsonFile(InputStream jsonStream, String fileName) {
        try {
            String ipfsHash = uploadToPinata(jsonStream, ContentType.APPLICATION_JSON, fileName);
            return CompletableFuture.completedFuture(ipfsHash);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.IPFS_UPLOAD_FAILED);
        }
    }

    private String uploadToPinata(InputStream stream, ContentType contentType, String fileName) {
        try {
            HttpPost post = new HttpPost(pinataConfig.getUploadUrl());

            post.setHeader("pinata_api_key", pinataConfig.getApiKey());
            post.setHeader("pinata_secret_api_key", pinataConfig.getApiSecret());

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", stream, contentType, fileName);
            post.setEntity(builder.build());

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(post)) {

                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> result = objectMapper.readValue(responseString, Map.class);
                return (String) result.get(IPFS_HASH_KEY);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.IPFS_UPLOAD_FAILED);
        }
    }

    public String cidToHttp(String cid) {
        return "https://" + pinataConfig.getGateway() + "/ipfs/" + cid;
    }
}
