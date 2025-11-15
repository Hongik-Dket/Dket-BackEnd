package com.example.demo.domain.ownership.repository;

import com.example.demo.domain.ownership.entity.Ownership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnershipRepository extends JpaRepository<Ownership, Long> {

    List<Ownership> findAllByOwnersAggregateIdOrderByOrdIndexAsc(Long aggregateId);

    @Query("""
        SELECT o.leafHex FROM Ownership o
        WHERE o.ownersAggregate.id = :aggregateId
            AND o.isActive = true
        ORDER BY o.ordIndex ASC
    """)
    List<String> findOwnerLeafHexes(@Param("aggregateId") Long aggregateId);

    @Query("""
        SELECT o FROM Ownership o
        WHERE o.ownersAggregate.sessionId = :sessionId
            AND o.user.id = :userId
            AND o.isActive = true
    """)
    Optional<Ownership> findBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId
    );

    @Query("""
        SELECT MAX(o.ordIndex)
        FROM Ownership o
        WHERE o.ownersAggregate.id = :aggregateId
    """)
    Integer findMaxOrdIndexByAggregateId(@Param("aggregateId") Long aggregateId);

    @Query("""
        SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END
        FROM Ownership o
        WHERE o.ownersAggregate.sessionId = :sessionId
          AND o.user.id = :userId
          AND o.isActive = true
    """)
    boolean existsActiveOwnershipBySessionIdAndUserId(@Param("sessionId") Long sessionId,
                                                      @Param("userId") Long userId);


}
