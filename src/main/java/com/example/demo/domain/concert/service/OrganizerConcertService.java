package com.example.demo.domain.concert.service;

import com.example.demo.domain.concert.dto.request.ConcertUploadDTO;
import com.example.demo.domain.concert.dto.response.OrganizerConcertDetailDTO;
import com.example.demo.domain.concert.dto.response.ResponseDTO;
import com.example.demo.domain.concert.dto.response.OrganizerSessionInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OrganizerConcertService {

    OrganizerConcertDetailDTO getConcertDetailForOrganizer(Long concertId);

    OrganizerSessionInfoDTO getSessionInfoForOrganizer(Long concertId, Long sessionId);

    ResponseDTO uploadConcert(ConcertUploadDTO request, MultipartFile banner, MultipartFile poster, List<MultipartFile> photocardList);

}
