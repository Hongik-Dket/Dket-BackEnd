package com.example.demo.domain.main.service;

import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.EventCardListDTO;

public interface BuyerHomeService {

    BuyerHomeResponseDTO getHomeForBuyer();

    EventCardListDTO getPopularEvents();

    EventCardListDTO getAppliedEvents();

    EventCardListDTO getPurchasedEvents();

    EventCardListDTO getAllEvents();
}