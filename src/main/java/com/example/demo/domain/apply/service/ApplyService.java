package com.example.demo.domain.apply.service;

import com.example.demo.domain.apply.DTO.ApplyResponseDTO;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.user.entity.User;

public interface ApplyService {

    void cancelWinnerTickets(Event event);

    ApplyResponseDTO applyToSession(Long eventId, Long sessionId);
}
