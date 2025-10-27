package com.example.demo.domain.resale.service;

import com.example.demo.domain.resale.dto.request.ResaleListingDTO;
import com.example.demo.domain.resale.dto.response.ResaleCardDTO;

import java.math.BigInteger;
import java.util.List;

public interface ResaleService {
    void createResale(Long ticketId, ResaleListingDTO request);

    void listResale(String ownerWalletAddress, BigInteger tokenId);

    List<ResaleCardDTO> getSessionResales(Long sessionId);
}
