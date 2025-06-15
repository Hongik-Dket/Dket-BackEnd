package com.example.demo.domain.metadata.converter;

import com.example.demo.domain.metadata.dto.PhotoCardDTO;
import com.example.demo.domain.metadata.entity.PhotoCard;

public class PhotoCardConverter {

    public static PhotoCardDTO toPhotoCardDTO(PhotoCard photoCard, String ipfsUrl) {
        return PhotoCardDTO.builder()
                .photoCardId(photoCard.getId())
                .ipfsUrl(ipfsUrl)
                .build();
    }
}
