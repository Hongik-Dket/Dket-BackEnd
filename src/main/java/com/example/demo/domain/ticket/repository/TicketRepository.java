package com.example.demo.domain.ticket.repository;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("""
        SELECT DISTINCT t.session.concert
        FROM Ticket t
        WHERE t.user.id = :buyerId
        ORDER BY 
            CASE WHEN t.session.concert.concertStatus = 'ENDED' THEN 1 ELSE 0 END ASC,
            t.session.concert.startDate ASC,
            t.session.concert.endDate DESC
    """)
    List<Concert> findPurchasedConcertsByBuyer(@Param("buyerId") Long buyerId, Pageable pageable);

    Optional<Ticket> findByTokenId(Long tokenId);

    int countBySessionIdAndPaidAtIsNotNull(Long sessionId);

    List<Ticket> findByUserIdAndSessionIdIn(Long userId, List<Long> sessionIds);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.session s " +
            "WHERE t.user = :user " +
            "ORDER BY CASE WHEN s.date >= :now THEN 0 ELSE 1 END ASC, s.date ASC")
    List<Ticket> findAllSortedByDateAndFutureFirst(@Param("user") User user, @Param("now") LocalDate now);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.session s " +
            "JOIN FETCH t.metadata m " +
            "JOIN FETCH m.photoCard pc " +
            "WHERE t.user = :user " +
            "ORDER BY CASE WHEN s.date >= :now THEN 0 ELSE 1 END, s.date ASC")
    List<Ticket> findAllSortedByDateAndFutureFirstWithPhotoCard(@Param("user") User user, @Param("now") LocalDate now);

    boolean existsByUserIdAndSessionId(Long userId, Long sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    Optional<Ticket> findByIdForUpdate(@Param("id") Long id);

    Optional<Ticket> findByIdAndUserId(Long id, Long userId);
}
