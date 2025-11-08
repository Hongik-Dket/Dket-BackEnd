package com.example.demo.domain.apply.repository;

import com.example.demo.domain.apply.entity.ApplicantsSnapshotItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicantsSnapshotItemRepository extends JpaRepository<ApplicantsSnapshotItem, Long> {

    List<ApplicantsSnapshotItem> findAllByApplicantsSnapshotIdOrderByOrdIndexAsc(Long snapshotId);

}
