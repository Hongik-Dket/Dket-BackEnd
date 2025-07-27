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

    private List<ConcertCardDTO> todayConcerts;

    private List<ConcertCardDTO> recentlyClosedApplyConcerts;

    private List<ConcertCardDTO> allConcerts;

    private List<ConcertCardDTO> endedConcerts;

}
