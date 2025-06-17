package com.example.demo.domain.event.service;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.dto.response.PriceWeiDTO;

import java.util.List;

public interface SessionService {

    void saveWinners(Long sessionId, List<String> winners);

    void completeDraw(Long sessionId);

    void createSessions(Event event);

    PriceWeiDTO getPriceWei(Long sessionId);

}
