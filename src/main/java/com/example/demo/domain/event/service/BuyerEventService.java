package com.example.demo.domain.event.service;

import com.example.demo.domain.event.dto.response.BuyerEventInfoDTO;

public interface BuyerEventService {
    BuyerEventInfoDTO getEventDetailForBuyer(Long eventId);
}
