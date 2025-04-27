package com.example.demo.domain.main.service;

import com.example.demo.domain.main.dto.EventCardListDTO;
import com.example.demo.domain.main.dto.OrganizerHomeResponseDTO;

public interface OrganizerHomeService {

    OrganizerHomeResponseDTO getHomeForOrganizer();

    EventCardListDTO getTodayEventsForOrganizer();

    EventCardListDTO getClosedEventsForOrganizer();

    EventCardListDTO getAllEventsForOrganizer();

}
