package com.example.demo.domain.main.service.impl;

import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.domain.main.converter.MainConverter;
import com.example.demo.domain.main.dto.BuyerHomeResponseDTO;
import com.example.demo.domain.main.dto.EventCardListDTO;
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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BuyerHomeServiceImpl implements BuyerHomeService {

    private final UserService userService;
    private final EventRepository eventRepository;
    private final ApplyRepository applyRepository;
    private final TicketRepository ticketRepository;

    @Override
    public BuyerHomeResponseDTO getHomeForBuyer() {
        User buyer = userService.getCurrentUser();
        Pageable top10 = PageRequest.of(0, 10);

        // 1. 인기 공연: 응모 중 + 응모자 많은 순 + 응모 마감 빠른 순
        List<Event> popularEvents = eventRepository.findPopularEvents(top10);

        // 2. 응모한 공연: 결제 안함 + 결제 마감 전
        List<Event> appliedEvents = applyRepository.findAppliedEventsByBuyer(buyer.getId(), top10);

        // 3. 구매한 공연: 결제 완료
        List<Event> purchasedEvents = ticketRepository.findPurchasedEventsByBuyer(buyer.getId(), top10);

        // 4. 전체 공연: 공연일 빠른 순 → 종료된 공연은 뒤로
        List<Event> entireEvents = eventRepository.findAllSorted(top10);

        return MainConverter.toBuyerHomeResponseDTO(popularEvents, appliedEvents, purchasedEvents, entireEvents);

    }

    @Override
    public EventCardListDTO getPopularEventsForBuyer() {
        Pageable top20 = PageRequest.of(0, 20);
        List<Event> events = eventRepository.findPopularEvents(top20);

        return MainConverter.toEventCardListDTO(events);
    }

    @Override
    public EventCardListDTO getAppliedEventsForBuyer() {
        User buyer = userService.getCurrentUser();
        List<Event> events = applyRepository.findAppliedEventsByBuyer(buyer.getId(), Pageable.unpaged());

        return MainConverter.toEventCardListDTO(events);
    }

    @Override
    public EventCardListDTO getPurchasedEventsForBuyer() {
        User buyer = userService.getCurrentUser();
        List<Event> events = ticketRepository.findPurchasedEventsByBuyer(buyer.getId(), Pageable.unpaged());

        return MainConverter.toEventCardListDTO(events);
    }

    @Override
    public EventCardListDTO getEntireEventsForBuyer() {
        List<Event> events = eventRepository.findAllSorted(Pageable.unpaged());

        return MainConverter.toEventCardListDTO(events);
    }
}