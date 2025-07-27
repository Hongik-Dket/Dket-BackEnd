package com.example.demo.domain.metadata.service.impl;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.metadata.entity.PhotoCard;
import com.example.demo.domain.metadata.service.PhotoCardService;
import com.example.demo.global.infra.ipfs.PinataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoCardServiceImpl implements PhotoCardService {

    private final PinataService pinataService;

    @Override
    @Transactional
    public void createPhotoCards(Concert concert, List<MultipartFile> images) {
        for (MultipartFile image : images) {
            PhotoCard photoCard = PhotoCard.builder()
                    .concert(concert)
                    .cid(pinataService.uploadPhotoCard(image))
                    .build();

            concert.addPhotoCard(photoCard);
        }
    }


}
