package com.example.demo.domain.event.service;

import com.example.demo.domain.event.dto.EventInfoDTO;
import com.example.demo.domain.event.dto.SessionInfoDTO;

public interface OrganizerEventService {

    EventInfoDTO getEventInfoForOrganizer(Long eventId);

    SessionInfoDTO getSessionInfoForOrganizer(Long eventId, Long sessionId);

}
