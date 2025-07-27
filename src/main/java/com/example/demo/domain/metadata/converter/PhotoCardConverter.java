package com.example.demo.domain.metadata.converter;

import com.example.demo.domain.metadata.dto.PhotoCardDTO;
import com.example.demo.domain.metadata.dto.PhotoCardDetailDTO;
import com.example.demo.domain.metadata.dto.PhotoCardInfoDTO;
import com.example.demo.domain.metadata.entity.PhotoCard;
import com.example.demo.domain.ticket.entity.Ticket;

public class PhotoCardConverter {

    public static PhotoCardDTO toPhotoCardDTO(Ticket ticket, String ipfsUrl) {
        return PhotoCardDTO.builder()
                .photoCardId(ticket.getMetadata().getPhotoCard().getId())
                .imageUrl(ipfsUrl)
                .ticketId(ticket.getId())
                .build();
    }

    public static PhotoCardDetailDTO toPhotoCardDetailDTO(Ticket ticket, String ipfsUrl, String nftUrl) {
        return PhotoCardDetailDTO.builder()
                .photoCardId(ticket.getMetadata().getPhotoCard().getId())
                .ticketId(ticket.getId())
                .imageUrl(ipfsUrl)
                .concertTitle(ticket.getSession().getConcert().getTitle())
                .sessionDate(ticket.getSession().getDate())
                .ticketNumber(ticket.getMetadata().getTicketNumber())
                .nftUrl(nftUrl)
                .build();
    }

    public static PhotoCardInfoDTO toPhotoCardInfoDTO(PhotoCard photoCard, String ipfsUrl) {
        return PhotoCardInfoDTO.builder()
                .photoCardId(photoCard.getId())
                .imageUrl(ipfsUrl)
                .build();
    }
}
