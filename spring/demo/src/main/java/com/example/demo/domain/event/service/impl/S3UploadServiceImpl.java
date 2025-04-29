package com.example.demo.domain.event.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.demo.domain.event.service.S3UploadService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class S3UploadServiceImpl implements S3UploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String saveFile(MultipartFile multipartFile) {

        String uniqueFileName = generateUniqueFileName(multipartFile.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try {
            amazonS3.putObject(bucket, uniqueFileName, multipartFile.getInputStream(), metadata);
        } catch (IOException e) {
            throw new CustomException(ErrorStatus.IMAGE_UPLOAD_FAILED);
        }

        return amazonS3.getUrl(bucket, uniqueFileName).toString();
    }
    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");

        if (lastDotIndex != -1)
            extension = originalFilename.substring(lastDotIndex);

        return UUID.randomUUID().toString() + extension;
    }

}