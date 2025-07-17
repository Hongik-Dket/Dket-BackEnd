package com.example.demo.domain.metadata.service;

import com.example.demo.domain.concert.entity.Concert;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoCardService {
    void createPhotoCards(Concert concert, List<MultipartFile> images);
}
