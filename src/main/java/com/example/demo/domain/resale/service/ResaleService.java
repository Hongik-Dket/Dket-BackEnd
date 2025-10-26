package com.example.demo.domain.resale.service;

import com.example.demo.domain.resale.dto.request.ResaleListingDTO;

public interface ResaleService {
    void listResale(Long ticketId, ResaleListingDTO request);
}
