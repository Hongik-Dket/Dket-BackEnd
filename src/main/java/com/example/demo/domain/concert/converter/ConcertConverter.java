package com.example.demo.domain.concert.converter;

import com.example.demo.domain.concert.dto.request.ConcertUploadDTO;
import com.example.demo.domain.concert.dto.response.BuyerConcertDetailDTO;
import com.example.demo.domain.concert.dto.response.BuyerSessionInfoDTO;
import com.example.demo.domain.concert.dto.response.OrganizerConcertDetailDTO;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.metadata.dto.PhotoCardInfoDTO;
import com.example.demo.domain.user.entity.User;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class ConcertConverter {

    public static OrganizerConcertDetailDTO toOrganizerConcertInfoDTO(Concert concert, List<PhotoCardInfoDTO> photoCardInfoDTOList) {
        return OrganizerConcertDetailDTO.builder()
                .concertId(concert.getId())
                .title(concert.getTitle())
                .posterUrl(concert.getPosterUrl())
                .location(concert.getLocation())
                .startDate(concert.getStartDate())
                .endDate(concert.getEndDate())
                .startTime(concert.getStartTime())
                .endTime(concert.getEndTime())
                .ageLimit(concert.getAgeLimit())
                .priceKrw(concert.getPriceKrw())
                .applyStart(concert.getApplyStart())
                .applyEnd(concert.getApplyEnd())
                .capacity(concert.getCapacity())
                .concertStatus(concert.getConcertStatus())
                .sessionIds(
                        concert.getSessions().stream()
                                .map(Session::getId)
                                .collect(Collectors.toList())
                )
                .description(concert.getDescription())
                .photoCardList(photoCardInfoDTOList)
                .build();
    }

    public static Concert toConcert(ConcertUploadDTO concertUploadDTO, User user, String bannerUrl, String posterUrl, BigInteger priceWei) {
        return Concert.builder()
                .organizer(user)
                .title(concertUploadDTO.getTitle())
                .ageLimit(concertUploadDTO.getAgeLimit())
                .location(concertUploadDTO.getLocation())
                .description(concertUploadDTO.getDescription())
                .startDate(concertUploadDTO.getStartDate())
                .endDate(concertUploadDTO.getEndDate())
                .startTime(concertUploadDTO.getStartTime())
                .endTime(concertUploadDTO.getEndTime())
                .priceKrw(concertUploadDTO.getPriceKrw())
                .capacity(concertUploadDTO.getCapacity())
                .applyStart(concertUploadDTO.getApplyStart())
                .applyEnd(concertUploadDTO.getApplyEnd())
                .bannerUrl(bannerUrl)
                .posterUrl(posterUrl)
                .priceWei(priceWei)
                .concertStatus(ConcertStatus.APPLY_NOT_OPENED)
                .build();
    }

    public static BuyerConcertDetailDTO toBuyerConcertInfoDTO(Concert concert, List<BuyerSessionInfoDTO> sessionList) {
        return BuyerConcertDetailDTO.builder()
                .concertId(concert.getId())
                .title(concert.getTitle())
                .description(concert.getDescription())
                .location(concert.getLocation())
                .ageLimit(concert.getAgeLimit())
                .priceKrw(concert.getPriceKrw())
                .applyStart(concert.getApplyStart())
                .applyEnd(concert.getApplyEnd())
                .startDate(concert.getStartDate())
                .endDate(concert.getEndDate())
                .startTime(concert.getStartTime())
                .endTime(concert.getEndTime())
                .posterUrl(concert.getPosterUrl())
                .capacity(concert.getCapacity())
                .concertStatus(concert.getConcertStatus())
                .sessionList(sessionList)
                .build();
    }

}
