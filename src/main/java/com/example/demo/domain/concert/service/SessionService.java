package com.example.demo.domain.concert.service;

import com.example.demo.domain.concert.dto.response.EntryCodeDTO;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.dto.response.PriceWeiAndChallengeDTO;

public interface SessionService {

    void createSessions(Concert concert);

    PriceWeiAndChallengeDTO getPriceWeiAndChallenge(Long sessionId);

    EntryCodeDTO getEntryCode(Long concertId, Long sessionId);

}
