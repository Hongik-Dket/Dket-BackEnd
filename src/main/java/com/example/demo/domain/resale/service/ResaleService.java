package com.example.demo.domain.resale.service;

import com.example.demo.domain.resale.dto.request.ResaleListingDTO;
import com.example.demo.domain.resale.dto.request.SignatureDTO;
import com.example.demo.domain.resale.dto.response.ResaleAuthDTO;
import com.example.demo.domain.resale.dto.response.ResaleCardDTO;
import com.example.demo.domain.resale.dto.response.ResaleDetailDTO;
import com.example.demo.domain.resale.dto.response.ResaleInfoWithChallengeDTO;

import java.math.BigInteger;
import java.util.List;

public interface ResaleService {
    ResaleInfoWithChallengeDTO createResale(Long ticketId, ResaleListingDTO request);

    void signResale(Long ticketId, SignatureDTO request);

    void listResale(String ownerWalletAddress, BigInteger tokenId);

    void completeResaleListing(Long resaleId);

    List<ResaleCardDTO> getSessionResales(Long sessionId);

    ResaleDetailDTO reserveResale(Long resaleId);

    void cancelResaleReservation(Long resaleId);

    ResaleAuthDTO authorizeResalePurchase(Long resaleId);

    void completeResalePurchase(Long resaleId);
}
