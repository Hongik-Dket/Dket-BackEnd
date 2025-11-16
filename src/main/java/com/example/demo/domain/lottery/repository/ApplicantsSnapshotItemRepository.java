package com.example.demo.domain.lottery.repository;

import com.example.demo.domain.lottery.entity.ApplicantsSnapshotItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicantsSnapshotItemRepository extends JpaRepository<ApplicantsSnapshotItem, Long> {

    List<ApplicantsSnapshotItem> findAllByApplicantsSnapshotIdOrderByOrdIndexAsc(Long snapshotId);

    @Query("""
        select i.apply.leafHex from ApplicantsSnapshotItem i
        where i.applicantsSnapshot.session.id = :sessionId
            and (i.apply.applyStatus = 'SELECTED' or i.apply.applyStatus = 'PAID')
        order by i.ordIndex asc
    """)
    List<String> findWinnerLeafHexes(@Param("sessionId") Long sessionId);

    @Query("""
        SELECT i FROM ApplicantsSnapshotItem i
        WHERE i.applicantsSnapshot.session.id = :sessionId
            AND i.apply.user.id = :userId
            AND i.apply.applyStatus = 'SELECTED'
    """)
    Optional<ApplicantsSnapshotItem> findBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId
    );

}
