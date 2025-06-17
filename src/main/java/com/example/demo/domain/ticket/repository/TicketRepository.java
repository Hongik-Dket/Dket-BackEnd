package com.example.demo.domain.ticket.repository;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("""
        SELECT DISTINCT t.session.event
        FROM Ticket t
        WHERE t.user.id = :buyerId
        ORDER BY 
            CASE WHEN t.session.event.eventStatus = 'ENDED' THEN 1 ELSE 0 END ASC,
            t.session.event.startDate ASC,
            t.session.event.endDate DESC
    """)
    List<Event> findPurchasedEventsByBuyer(@Param("buyerId") Long buyerId, Pageable pageable);

    Optional<Ticket> findByTokenId(Long tokenId);

    Optional<Ticket> findByMetadata(Metadata metadata);

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

    Optional<Ticket> findByIdAndUserId(Long id, Long userId);
}
