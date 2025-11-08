package com.example.demo.domain.concert.service;

import com.example.demo.domain.concert.dto.response.EntryCodeDTO;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.dto.response.PriceWeiDTO;
import com.example.demo.domain.concert.entity.Session;

import java.util.List;

public interface SessionService {

    void commitApplicants(Session session);

    void drawWinners(Long sessionId);

    void saveWinners(Long sessionId, List<String> winners);

    void completeDraw(Long sessionId);

    void createSessions(Concert concert);

    PriceWeiDTO getPriceWei(Long sessionId);

    EntryCodeDTO getEntryCode(Long concertId, Long sessionId);

}
