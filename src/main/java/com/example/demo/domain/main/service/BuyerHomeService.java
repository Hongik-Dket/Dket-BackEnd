package com.example.demo.domain.main.service;

import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.ConcertCardDTO;
import com.example.demo.domain.main.dto.ConcertCardListDTO;

import java.util.List;

public interface BuyerHomeService {

    BuyerHomeResponseDTO getHomeForBuyer();

    ConcertCardListDTO getPopularConcertsForBuyer();

    ConcertCardListDTO getAppliedConcertsForBuyer();

    ConcertCardListDTO getPurchasedConcertsForBuyer();

    ConcertCardListDTO getEntireConcertsForBuyer();

    List<ConcertCardDTO> searchConcert(String keyword);
    
}