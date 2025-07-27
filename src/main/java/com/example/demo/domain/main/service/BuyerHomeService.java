package com.example.demo.domain.main.service;

import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.ConcertCardListDTO;

public interface BuyerHomeService {

    BuyerHomeResponseDTO getHomeForBuyer();

    ConcertCardListDTO getPopularConcertsForBuyer();

    ConcertCardListDTO getAppliedConcertsForBuyer();

    ConcertCardListDTO getPurchasedConcertsForBuyer();

    ConcertCardListDTO getEntireConcertsForBuyer();
    
}