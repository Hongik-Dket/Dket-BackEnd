package com.example.demo.global.infra.awsS3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
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
public class S3UploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String saveFile(MultipartFile multipartFile) {

        String uniqueFileName = generateUniqueFileName(multipartFile.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());

        String contentType = multipartFile.getContentType();
        if (contentType == null)
            contentType = "application/octet-stream";
        metadata.setContentType(contentType);

        try {
            amazonS3.putObject(bucket, uniqueFileName, multipartFile.getInputStream(), metadata);
        } catch (IOException e) {
            throw new CustomException(ErrorStatus.IMAGE_UPLOAD_FAILED);
        }

        return amazonS3.getUrl(bucket, uniqueFileName).toString();
    }

    public String generateUniqueFileName(String originalFilename) {
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");

        if (lastDotIndex != -1)
            extension = originalFilename.substring(lastDotIndex);

        return UUID.randomUUID().toString() + extension;
    }

}