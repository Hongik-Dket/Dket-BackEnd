package com.example.demo.domain.event.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3UploadService {

    String saveFile(MultipartFile multipartFile);

}
