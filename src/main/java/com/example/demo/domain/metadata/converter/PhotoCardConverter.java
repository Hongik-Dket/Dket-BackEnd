package com.example.demo.domain.metadata.converter;

import com.example.demo.domain.metadata.dto.PhotoCardDTO;
import com.example.demo.domain.metadata.dto.PhotoCardInfoDTO;
import com.example.demo.domain.metadata.entity.PhotoCard;
import com.example.demo.domain.ticket.entity.Ticket;

public class PhotoCardConverter {

    public static PhotoCardDTO toPhotoCardDTO(Ticket ticket) {
        return PhotoCardDTO.builder()
                .photoCardId(ticket.getMetadata().getPhotoCard().getId())
                .imageUrl(ticket.getMetadata().getPhotoCard().getUrl())
                .ticketId(ticket.getId())
                .build();
    }

    public static PhotoCardInfoDTO toPhotoCardInfoDTO(PhotoCard photoCard) {
        return PhotoCardInfoDTO.builder()
                .photoCardId(photoCard.getId())
                .imageUrl(photoCard.getUrl())
                .build();
    }
}
