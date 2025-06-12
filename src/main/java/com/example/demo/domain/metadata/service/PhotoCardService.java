package com.example.demo.domain.metadata.service;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.metadata.entity.PhotoCard;
import com.example.demo.global.infra.ipfs.PinataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoCardService {

    private final PinataService pinataService;

    @Transactional
    public void createPhotoCards(Event event, List<MultipartFile> images) {
        for (MultipartFile image : images) {
            PhotoCard photoCard = PhotoCard.builder()
                    .event(event)
                    .ipfsCid(pinataService.uploadSingleFile(image))
                    .build();

            event.addPhotoCard(photoCard);
        }
    }


}
