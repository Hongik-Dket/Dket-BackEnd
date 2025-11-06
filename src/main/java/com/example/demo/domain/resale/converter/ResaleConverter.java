package com.example.demo.domain.resale.converter;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.resale.dto.response.ResaleCardDTO;
import com.example.demo.domain.resale.dto.response.ResaleDetailDTO;
import com.example.demo.domain.resale.dto.response.ResaleInfoDTO;
import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.user.entity.User;

import java.math.BigInteger;

public class ResaleConverter {

    public static Resale toResale(Ticket ticket, User seller, int priceKrw, BigInteger priceWei){
        return Resale.builder()
                .ticket(ticket)
                .session(ticket.getSession())
                .seller(seller)
                .priceKrw(priceKrw)
                .priceWei(priceWei)
                .resaleStatus(ResaleStatus.LISTING)
                .build();
    }

    public static ResaleInfoDTO toResaleInfoDTO(Resale resale){
        return ResaleInfoDTO.builder()
                .resaleId(resale.getId())
                .tokenId(resale.getTicket().getTokenId())
                .build();
    }

    public static ResaleCardDTO toResaleCardDTO(Resale resale){
        return ResaleCardDTO.builder()
                .resaleId(resale.getId())
                .ticketId(resale.getTicket().getId())
                .priceKrw(resale.getPriceKrw())
                .seatCode(resale.getTicket().getMetadata().getSeatCode())
                .resaleStatus(resale.getResaleStatus())
                .photoCardUrl(resale.getTicket().getMetadata().getPhotoCard().getUrl())
                .build();
    }

    public static ResaleDetailDTO toResaleDetailDTO(Resale resale){
        Concert concert = resale.getSession().getConcert();

        return ResaleDetailDTO.builder()
                .resaleId(resale.getId())
                .concertTitle(concert.getTitle())
                .location(concert.getLocation())
                .date(resale.getSession().getDate())
                .startTime(concert.getStartTime())
                .seatCode(resale.getTicket().getMetadata().getSeatCode())
                .originalPrice(concert.getPriceKrw())
                .priceKrw(resale.getPriceKrw())
                .priceWei(resale.getPriceWei())
                .photoCardUrl(resale.getTicket().getMetadata().getPhotoCard().getUrl())
                .build();
    }
}
