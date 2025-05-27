package com.example.demo.domain.event.service;

import com.example.demo.global.infra.blockchain.dto.SessionOnDrawnRequestDTO;

public interface SessionService {

    void saveWinners(SessionOnDrawnRequestDTO request);

}
