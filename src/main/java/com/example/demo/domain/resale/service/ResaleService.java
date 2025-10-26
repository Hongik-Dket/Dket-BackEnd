package com.example.demo.domain.resale.service;

import com.example.demo.domain.resale.dto.request.ResaleListingDTO;

import java.math.BigInteger;

public interface ResaleService {
    void createResale(Long ticketId, ResaleListingDTO request);

    void listResale(String ownerWalletAddress, BigInteger tokenId);
}
