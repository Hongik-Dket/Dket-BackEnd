package com.example.demo.domain.main.service;

import com.example.demo.domain.main.dto.ConcertCardListDTO;
import com.example.demo.domain.main.dto.OrganizerHomeResponseDTO;

public interface OrganizerHomeService {

    OrganizerHomeResponseDTO getHomeForOrganizer();

    ConcertCardListDTO getTodayConcertsForOrganizer();

    ConcertCardListDTO getClosedConcertsForOrganizer();

    ConcertCardListDTO getAllConcertsForOrganizer();

}
