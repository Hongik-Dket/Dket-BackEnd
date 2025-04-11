package com.example.demo.domain.event.repository;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findById(Long id);

    List<Event> findByOrganizerAndEventStatus(User organizer, EventStatus eventStatus);

    List<Event> findByOrganizerAndApplyEndBetweenOrderByApplyEndDesc(User organizer, LocalDateTime start, LocalDateTime end);

    List<Event> findByOrganizerAndEventStatusNotOrderByStartDateAsc(User organizer, EventStatus status);

}
