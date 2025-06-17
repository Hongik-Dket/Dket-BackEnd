package com.example.demo.domain.apply.repository;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Apply a SET a.applyStatus = :newStatus " +
            "WHERE a.session.id = :sessionId AND a.applyStatus = :currentStatus")
    int batchUpdateApplyStatusBySessionId(@Param("sessionId") Long sessionId,
                                          @Param("currentStatus") ApplyStatus currentStatus,
                                          @Param("newStatus") ApplyStatus newStatus);

    @Modifying
    @Query("UPDATE Apply a SET a.applyStatus = :newStatus " +
            "WHERE a.session.id = :sessionId AND a.user.walletAddress IN :walletAddresses")
    int batchUpdateApplyStatusBySessionIdAndWalletAddresses(
            @Param("sessionId") Long sessionId,
            @Param("walletAddresses") List<String> walletAddresses,
            @Param("newStatus") ApplyStatus newStatus
    );

    @Modifying
    @Query("UPDATE Apply a SET a.applyStatus = :newStatus " +
            "WHERE a.session.id = :sessionId AND a.user.walletAddress NOT IN :walletAddresses")
    int batchUpdateStatusExceptWallets(
            @Param("sessionId") Long sessionId,
            @Param("walletAddresses") List<String> walletAddresses,
            @Param("newStatus") ApplyStatus newStatus
    );

    @Query("""
    SELECT DISTINCT a.session.event
    FROM Apply a
    WHERE a.user.id = :buyerId
      AND a.applyStatus IN ('APPLIED', 'SELECTED', 'NOT_SELECTED')
      AND a.session.event.eventStatus IN ('APPLY_OPEN', 'APPLY_CLOSED')
    ORDER BY
        CASE WHEN a.session.event.applyEnd < CURRENT_TIMESTAMP THEN 0 ELSE 1 END,
        a.session.event.applyEnd ASC
    """)
    List<Event> findAppliedEventsByBuyer(@Param("buyerId") Long buyerId, Pageable pageable);

    Optional<Apply> findBySessionIdAndUserId(Long sessionId, Long userId);

    boolean existsByUserIdAndSessionId(Long userId, Long sessionId);

    List<Apply> findByUserIdAndSessionIdIn(Long userId, List<Long> sessionIds);
}
