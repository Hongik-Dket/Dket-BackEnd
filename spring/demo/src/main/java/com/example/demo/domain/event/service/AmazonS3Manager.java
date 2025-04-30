package com.example.demo.domain.event.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.demo.global.config.AmazonConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {

    private final AmazonS3 amazonS3;

    private final AmazonConfig amazonConfig;

    public String uploadFile(String keyName, MultipartFile file){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        // 파일의 확장자를 추출
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        // 확장자를 포함한 최종 keyName 생성
        String finalKeyName = keyName + fileExtension;

        try {
            amazonS3.putObject(new PutObjectRequest(amazonConfig.getBucket(), finalKeyName, file.getInputStream(), metadata));
        } catch (IOException e) {
            log.error("Error at AmazonS3Manager uploadFile: {}", e.getMessage());
        }

        return amazonS3.getUrl(amazonConfig.getBucket(), finalKeyName).toString();
    }

    public void deleteFile(String imageUrl) {
        String keyName = amazonConfig.extractKeyFromUrl(imageUrl);

        amazonS3.deleteObject(amazonConfig.getBucket(), keyName);
    }

    public String generateKeyName() {
        return UUID.randomUUID().toString();
    }

}