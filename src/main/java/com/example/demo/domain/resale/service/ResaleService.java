package com.example.demo.domain.resale.service;

import com.example.demo.domain.resale.dto.request.ResaleListingDTO;
import com.example.demo.domain.resale.dto.response.ResaleAuthDTO;
import com.example.demo.domain.resale.dto.response.ResaleCardDTO;
import com.example.demo.domain.resale.dto.response.ResaleDetailDTO;
import com.example.demo.domain.resale.dto.response.ResaleInfoDTO;

import java.math.BigInteger;
import java.util.List;

public interface ResaleService {
    ResaleInfoDTO createResale(Long ticketId, ResaleListingDTO request);

    void listResale(String ownerWalletAddress, BigInteger tokenId);

    void completeResaleListing(Long resaleId);

    List<ResaleCardDTO> getSessionResales(Long sessionId);

    ResaleDetailDTO reserveResale(Long resaleId);

    void cancelResaleReservation(Long resaleId);

    ResaleAuthDTO authorizeResalePurchase(Long resaleId);

    void completeResalePurchase(Long resaleId);
}
