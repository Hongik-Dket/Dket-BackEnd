package com.example.demo.domain.resale.converter;

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
}
