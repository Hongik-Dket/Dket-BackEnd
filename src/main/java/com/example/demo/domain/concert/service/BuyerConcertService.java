package com.example.demo.domain.concert.service;

import com.example.demo.domain.concert.dto.response.BuyerConcertDetailDTO;

public interface BuyerConcertService {
    BuyerConcertDetailDTO getConcertDetailForBuyer(Long concertId);
}
