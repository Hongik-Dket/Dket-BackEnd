package com.example.demo.domain.apply.service;

import com.example.demo.domain.apply.dto.ApplyResponseDTO;
import com.example.demo.domain.concert.entity.Concert;

public interface ApplyService {

    void cancelWinnerTickets(Concert concert);

    ApplyResponseDTO applyToSession(Long concertId, Long sessionId);
}
