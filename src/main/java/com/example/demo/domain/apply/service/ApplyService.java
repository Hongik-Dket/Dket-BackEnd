package com.example.demo.domain.apply.service;

import com.example.demo.domain.apply.dto.ApplyResponseDTO;
import com.example.demo.domain.event.entity.Event;

public interface ApplyService {

    void cancelWinnerTickets(Event event);

    ApplyResponseDTO applyToSession(Long eventId, Long sessionId);
}
