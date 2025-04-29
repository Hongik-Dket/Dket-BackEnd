package com.example.demo.domain.event.service;

import com.example.demo.domain.event.dto.request.EventUploadDTO;
import com.example.demo.domain.event.dto.response.EventInfoDTO;
import com.example.demo.domain.event.dto.response.ResponseDTO;
import com.example.demo.domain.event.dto.response.SessionInfoDTO;

public interface OrganizerEventService {

    EventInfoDTO getEventInfoForOrganizer(Long eventId);

    SessionInfoDTO getSessionInfoForOrganizer(Long eventId, Long sessionId);

    ResponseDTO uploadEvent(EventUploadDTO request);

}
