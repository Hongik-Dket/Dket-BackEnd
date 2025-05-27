package com.example.demo.domain.apply.service;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private final ApplyRepository applyRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void cancelWinnerTickets(Event event) {

        List<Long> sessionIds = eventRepository.findSessionIdsByEventId(event.getId());

        for (Long sessionId : sessionIds)
            applyRepository.batchUpdateApplyStatusBySessionId(sessionId, ApplyStatus.SELECTED, ApplyStatus.CANCELED);

    }


}
