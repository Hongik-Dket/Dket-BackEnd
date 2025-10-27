package com.example.demo.domain.resale.converter;

import com.example.demo.domain.resale.dto.response.ResaleCardDTO;
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
                .resaleStatus(ResaleStatus.AVAILABLE)
                .build();
    }

    public static ResaleCardDTO toResaleCardDTO(Resale resale, String photoCardUrl){
        return ResaleCardDTO.builder()
                .resaleId(resale.getId())
                .ticketId(resale.getTicket().getId())
                .priceKrw(resale.getPriceKrw())
                .seatCode(resale.getTicket().getMetadata().getSeatCode())
                .resaleStatus(resale.getResaleStatus())
                .photoCardUrl(photoCardUrl)
                .build();
    }
}
