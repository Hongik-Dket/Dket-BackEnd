package com.example.demo.domain.lottery.repository;

import com.example.demo.domain.lottery.entity.ApplicantsSnapshotItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicantsSnapshotItemRepository extends JpaRepository<ApplicantsSnapshotItem, Long> {

    List<ApplicantsSnapshotItem> findAllByApplicantsSnapshotIdOrderByOrdIndexAsc(Long snapshotId);

    @Query("""
        select i.apply.leafHex from ApplicantsSnapshotItem i
        where i.applicantsSnapshot.session.id = :sessionId
            and i.apply.applyStatus = 'SELECTED'
        order by i.ordIndex asc
    """)
    List<String> findWinnerLeafHexes(@Param("sessionId") Long sessionId);

}
