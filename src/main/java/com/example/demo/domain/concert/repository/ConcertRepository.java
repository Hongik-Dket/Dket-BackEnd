package com.example.demo.domain.concert.repository;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.enums.ConcertStatus;
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
public interface ConcertRepository extends JpaRepository<Concert, Long> {
    Optional<Concert> findById(Long id);

    Page<Concert> findByOrganizerIdAndConcertStatus(Long organizerId, ConcertStatus concertStatus, Pageable pageable);

    Page<Concert> findByOrganizerIdAndConcertStatusNot(Long organizer, ConcertStatus concertStatus, Pageable pageable);

    List<Concert> findByConcertStatusNotIn(List<ConcertStatus> excludedStatuses);

    @Query("SELECT s.id FROM Session s WHERE s.concert.id = :concertId")
    List<Long> findSessionIdsByConcertId(@Param("concertId") Long concertId);

    // 1. 인기 공연 조회 (응모 중인 공연 중 응모자 수 많은 순 + 응모 마감 빠른 순)
    @Query("""
        SELECT e FROM Concert e
        WHERE e.concertStatus = 'APPLY_OPEN'
        ORDER BY (
            SELECT COUNT(a) FROM Apply a
            WHERE a.session.concert = e
        ) DESC, e.applyEnd ASC
    """)
    List<Concert> findPopularConcerts(Pageable pageable);

    // 2. 전체 공연 조회 (시작일 빠른 순 + 종료 공연은 뒤에서 최근 종료 순)
    @Query("""
        SELECT e FROM Concert e
        ORDER BY
            CASE WHEN e.concertStatus = 'ENDED' THEN 1 ELSE 0 END ASC,
            e.startDate ASC,
            e.endDate DESC
    """)
    List<Concert> findAllSorted(Pageable pageable);

    @Query("""
    SELECT e FROM Concert e
    WHERE e.organizer.id = :organizerId
      AND (
            (e.applyEnd BETWEEN :from AND :to)
            OR e.concertStatus = 'APPLY_CLOSED'
      )
    ORDER BY e.applyEnd DESC
""")
    Page<Concert> findRecentlyClosedApply(
            @Param("organizerId") Long organizerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
    SELECT e FROM Concert e
    WHERE e.organizer.id = :organizerId
    ORDER BY
        CASE WHEN e.concertStatus = 'ENDED' THEN 1 ELSE 0 END ASC,
        e.startDate ASC,
        e.endDate DESC
    """)
    Page<Concert> findSortedForOrganizer(@Param("organizerId") Long organizerId, Pageable pageable);
}
