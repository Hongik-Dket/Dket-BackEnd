package com.example.demo.domain.main.service.impl;

import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.domain.concert.repository.ConcertRepository;
import com.example.demo.domain.main.converter.MainConverter;
import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.ConcertCardDTO;
import com.example.demo.domain.main.dto.ConcertCardListDTO;
import com.example.demo.domain.main.service.BuyerHomeService;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.domain.main.converter.MainConverter.toConcertCardDTOList;
import static com.example.demo.global.util.StringUtil.normalize;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BuyerHomeServiceImpl implements BuyerHomeService {

    private final UserService userService;
    private final ConcertRepository concertRepository;
    private final ApplyRepository applyRepository;
    private final TicketRepository ticketRepository;

    @Override
    public BuyerHomeResponseDTO getHomeForBuyer() {
        User buyer = userService.getCurrentUser();
        Pageable top10 = PageRequest.of(0, 10);

        // 1. 인기 공연: 응모 중 + 응모자 많은 순 + 응모 마감 빠른 순
        List<Concert> popularConcerts = concertRepository.findPopularConcerts(top10);

        // 2. 응모한 공연: 결제 안함 + 결제 마감 전
        List<Concert> appliedConcerts = applyRepository.findAppliedConcertsByBuyer(buyer.getId(), top10);

        // 3. 구매한 공연: 결제 완료
        List<Concert> purchasedConcerts = ticketRepository.findPurchasedConcertsByBuyer(buyer.getId(), top10);

        // 4. 전체 공연: 공연일 빠른 순 → 종료된 공연은 뒤로
        List<Concert> entireConcerts = concertRepository.findAllSorted(top10);

        return MainConverter.toBuyerHomeResponseDTO(popularConcerts, appliedConcerts, purchasedConcerts, entireConcerts);

    }

    @Override
    public ConcertCardListDTO getPopularConcertsForBuyer() {
        Pageable top20 = PageRequest.of(0, 20);
        List<Concert> concerts = concertRepository.findPopularConcerts(top20);

        return MainConverter.toConcertCardListDTO(concerts);
    }

    @Override
    public ConcertCardListDTO getAppliedConcertsForBuyer() {
        User buyer = userService.getCurrentUser();
        List<Concert> concerts = applyRepository.findAppliedConcertsByBuyer(buyer.getId(), Pageable.unpaged());

        return MainConverter.toConcertCardListDTO(concerts);
    }

    @Override
    public ConcertCardListDTO getPurchasedConcertsForBuyer() {
        User buyer = userService.getCurrentUser();
        List<Concert> concerts = ticketRepository.findPurchasedConcertsByBuyer(buyer.getId(), Pageable.unpaged());

        return MainConverter.toConcertCardListDTO(concerts);
    }

    @Override
    public ConcertCardListDTO getEntireConcertsForBuyer() {
        List<Concert> concerts = concertRepository.findAllSorted(Pageable.unpaged());

        return MainConverter.toConcertCardListDTO(concerts);
    }

    @Override
    public List<ConcertCardDTO> searchConcert(String keyword) {
        List<Concert> concerts = concertRepository.searchByTitleWithChronology(normalize(keyword), ConcertStatus.ENDED);

        return toConcertCardDTOList(concerts, true);
    }
}