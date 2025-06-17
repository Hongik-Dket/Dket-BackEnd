package com.example.demo.domain.event.repository;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findById(Long id);

    Page<Event> findByOrganizerIdAndEventStatus(Long organizerId, EventStatus eventStatus, Pageable pageable);

    Page<Event> findByOrganizerIdAndEventStatusNot(Long organizer, EventStatus eventStatus, Pageable pageable);

    List<Event> findByEventStatusNotIn(List<EventStatus> excludedStatuses);

    @Query("SELECT s.id FROM Session s WHERE s.event.id = :eventId")
    List<Long> findSessionIdsByEventId(@Param("eventId") Long eventId);

    // 1. 인기 공연 조회 (응모 중인 공연 중 응모자 수 많은 순 + 응모 마감 빠른 순)
    @Query("""
        SELECT e FROM Event e
        WHERE e.eventStatus = 'APPLY_OPEN'
        ORDER BY (
            SELECT COUNT(a) FROM Apply a
            WHERE a.session.event = e
        ) DESC, e.applyEnd ASC
    """)
    List<Event> findPopularEvents(Pageable pageable);

    // 2. 전체 공연 조회 (시작일 빠른 순 + 종료 공연은 뒤에서 최근 종료 순)
    @Query("""
        SELECT e FROM Event e
        ORDER BY
            CASE WHEN e.eventStatus = 'ENDED' THEN 1 ELSE 0 END ASC,
            e.startDate ASC,
            e.endDate DESC
    """)
    List<Event> findAllSorted(Pageable pageable);

    @Query("""
    SELECT e FROM Event e
    WHERE e.organizer.id = :organizerId
      AND (
            (e.applyEnd BETWEEN :from AND :to)
            OR e.eventStatus = 'APPLY_CLOSED'
      )
    ORDER BY e.applyEnd DESC
""")
    Page<Event> findRecentlyClosedApply(
            @Param("organizerId") Long organizerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
    SELECT e FROM Event e
    WHERE e.organizer.id = :organizerId
    ORDER BY
        CASE WHEN e.eventStatus = 'ENDED' THEN 1 ELSE 0 END ASC,
        e.startDate ASC,
        e.endDate DESC
    """)
    Page<Event> findSortedForOrganizer(@Param("organizerId") Long organizerId, Pageable pageable);
}
