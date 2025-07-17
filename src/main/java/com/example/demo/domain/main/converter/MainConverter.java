package com.example.demo.domain.main.converter;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.ConcertCardDTO;
import com.example.demo.domain.main.dto.ConcertCardListDTO;
import com.example.demo.domain.main.dto.OrganizerHomeResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public class MainConverter {

    private static ConcertCardDTO toConcertCardDTO(Concert concert, boolean poster) {
        String image;

        if (poster)
            image = concert.getPosterUrl();
        else
            image = concert.getBannerUrl();

        return ConcertCardDTO.builder()
                .concertId(concert.getId())
                .title(concert.getTitle())
                .location(concert.getLocation())
                .startDate(concert.getStartDate())
                .endDate(concert.getEndDate())
                .concertStatus(concert.getConcertStatus())
                .imageUrl(image)
                .build();
    }

    private static List<ConcertCardDTO> toConcertCardDTOList(List<Concert> concerts, boolean poster) {
        return concerts.stream()
                .map((Concert concert) -> toConcertCardDTO(concert, poster))
                .toList();
    }

    private static List<ConcertCardDTO> toConcertCardDTOListFromPage(Page<Concert> concerts, boolean poster) {
        return toConcertCardDTOList(concerts.getContent(), poster);
    }

    public static OrganizerHomeResponseDTO toOrganizerHomeResponseDTO(
            Page<Concert> todayConcerts,
            Page<Concert> recentlyClosedApplyConcerts,
            Page<Concert> otherConcerts,
            Page<Concert> endedConcerts
    ) {
        return OrganizerHomeResponseDTO.builder()
                .todayConcerts(toConcertCardDTOListFromPage(todayConcerts, true))
                .recentlyClosedApplyConcerts(toConcertCardDTOListFromPage(recentlyClosedApplyConcerts, true))
                .allConcerts(toConcertCardDTOListFromPage(otherConcerts, true))
                .endedConcerts(toConcertCardDTOListFromPage(endedConcerts, true))
                .build();
    }

    public static ConcertCardListDTO toConcertCardListDTO(List<Concert> concerts) {
        return ConcertCardListDTO.builder()
                .concertCardList(toConcertCardDTOList(concerts, false))
                .build();
    }

    // 구매자 홈 기능
    public static BuyerHomeResponseDTO toBuyerHomeResponseDTO(
            List<Concert> popularConcerts,
            List<Concert> appliedConcerts,
            List<Concert> purchasedConcerts,
            List<Concert> entireConcerts
    ) {
        return new BuyerHomeResponseDTO(
                toConcertCardDTOList(popularConcerts, true),
                toConcertCardDTOList(appliedConcerts, true),
                toConcertCardDTOList(purchasedConcerts, true),
                toConcertCardDTOList(entireConcerts, true)
        );
    }
}
