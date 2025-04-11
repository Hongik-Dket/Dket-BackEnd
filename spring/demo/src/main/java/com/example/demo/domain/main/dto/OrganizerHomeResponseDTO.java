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
    private int todayEventCount;

    private List<EventCardDTO> recentlyClosedApplyEvents;
    private int recentlyClosedApplyEventCount;

    private List<EventCardDTO> otherEvents;
    private int otherEventCount;

}
