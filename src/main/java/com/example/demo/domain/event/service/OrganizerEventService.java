package com.example.demo.domain.event.service;

import com.example.demo.domain.event.dto.request.EventUploadDTO;
import com.example.demo.domain.event.dto.response.OrganizerEventInfoDTO;
import com.example.demo.domain.event.dto.response.ResponseDTO;
import com.example.demo.domain.event.dto.response.OrganizerSessionInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OrganizerEventService {

    OrganizerEventInfoDTO getEventInfoForOrganizer(Long eventId);

    OrganizerSessionInfoDTO getSessionInfoForOrganizer(Long eventId, Long sessionId);

    ResponseDTO uploadEvent(EventUploadDTO request, MultipartFile banner, MultipartFile poster, List<MultipartFile> photocardList);

}
