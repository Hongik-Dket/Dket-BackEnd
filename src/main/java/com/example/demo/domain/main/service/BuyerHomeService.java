package com.example.demo.domain.main.service;

import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.EventCardListDTO;
import org.springframework.data.domain.Pageable;

public interface BuyerHomeService {

    BuyerHomeResponseDTO getHomeForBuyer();

    EventCardListDTO getPopularEventsForBuyer();

    EventCardListDTO getAppliedEventsForBuyer();

    EventCardListDTO getPurchasedEventsForBuyer();

    EventCardListDTO getEntireEventsForBuyer();
    
}