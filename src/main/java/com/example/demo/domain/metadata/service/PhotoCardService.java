package com.example.demo.domain.metadata.service;

import com.example.demo.domain.event.entity.Event;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoCardService {
    void createPhotoCards(Event event, List<MultipartFile> images);
}
