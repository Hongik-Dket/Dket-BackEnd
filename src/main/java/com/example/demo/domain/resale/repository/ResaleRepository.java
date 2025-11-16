package com.example.demo.domain.resale.repository;

import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.enums.ResaleStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResaleRepository extends JpaRepository<Resale, Long> {

    boolean existsByTicketIdAndResaleStatusIn(Long ticketId, Collection<ResaleStatus> resaleStatus);

    Optional<Resale> findBySellerWalletAddressAndTicketTokenIdAndResaleStatusIn(
            String walletAddress,
            BigInteger tokenId,
            Collection<ResaleStatus> resaleStatuses
    );

    boolean existsByTicketIdAndSellerIdAndResaleStatusIn(
            Long ticketId,
            Long sellerId,
            Collection<ResaleStatus> statuses
    );

    @Query("""
    SELECT r
    FROM Resale r
    WHERE r.session.id = :sessionId
      AND r.resaleStatus IN ('AVAILABLE', 'RESERVED')
    ORDER BY
      CASE r.resaleStatus
        WHEN 'AVAILABLE' THEN 1
        WHEN 'RESERVED' THEN 2
      END,
      r.ticket.metadata.seatCode ASC
    """)
    List<Resale> findSessionResalesSorted(@Param("sessionId") Long sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Resale r where r.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    Optional<Resale> findByIdForUpdate(@Param("id") Long id);

    Optional<Resale> findById(Long id);

    List<Resale> findByResaleStatusIn(Collection<ResaleStatus> resaleStatuses);

    @Query("""
        SELECT r.id FROM Resale r
        WHERE r.session.id = :sessionId AND r.resaleStatus IN :statuses
    """)
    List<Long> findIdsBySessionIdAndStatus(
            @Param("sessionId") Long sessionId,
            @Param("statuses") Collection<ResaleStatus> statuses
    );

    @Modifying
    @Query("""
        UPDATE Resale r SET r.resaleStatus = :newStatus
        WHERE r.session.id = :sessionId AND r.resaleStatus IN :statuses
    """)
    void updateBySessionIdAndResaleStatusIn(
            @Param("sessionId") Long sessionId,
            @Param("statuses") Collection<ResaleStatus> statuses,
            @Param("newStatus") ResaleStatus newStatus
    );
}
