package com.example.demo.domain.event.service;

import com.example.demo.domain.event.dto.response.BuyerEventDetailDTO;

public interface BuyerEventService {
    BuyerEventDetailDTO getEventDetailForBuyer(Long eventId);
}
