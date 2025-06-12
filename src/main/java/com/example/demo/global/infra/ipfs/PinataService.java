package com.example.demo.global.infra.ipfs;

import com.example.demo.global.infra.awsS3.S3UploadService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PinataService {

    private final PinataConfig pinataConfig;
    private final S3UploadService s3UploadService;

    private static final String IPFS_HASH_KEY = "IpfsHash";

    public String uploadSingleFile(MultipartFile file) {

        try {
            HttpPost post = new HttpPost(pinataConfig.getUploadUrl());

            post.setHeader("pinata_api_key", pinataConfig.getApiKey());
            post.setHeader("pinata_secret_api_key", pinataConfig.getApiSecret());

            String filename = s3UploadService.generateUniqueFileName(file.getOriginalFilename());

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", file.getInputStream(), ContentType.DEFAULT_BINARY, filename);
            post.setEntity(builder.build());

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(post)) {

                String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> result = objectMapper.readValue(responseString, Map.class);
                String cid = (String) result.get(IPFS_HASH_KEY);

                return cid;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(ErrorStatus.IPFS_UPLOAD_FAILED);
        }
    }

}
