package com.example.demo.domain.concert.service;

import com.example.demo.domain.concert.dto.response.EntryCodeDTO;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.dto.response.PriceWeiDTO;

public interface SessionService {

    void createSessions(Concert concert);

    PriceWeiDTO getPriceWei(Long sessionId);

    EntryCodeDTO getEntryCode(Long concertId, Long sessionId);

}
