package com.example.demo.domain.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerHomeResponseDTO {

    private List<EventCardDTO> todayEvents;

    private List<EventCardDTO> recentlyClosedApplyEvents;

    private List<EventCardDTO> allEvents;

    private List<EventCardDTO> endedEvents;

}
