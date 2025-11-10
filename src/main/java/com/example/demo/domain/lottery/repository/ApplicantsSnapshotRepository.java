package com.example.demo.domain.lottery.repository;

import com.example.demo.domain.lottery.entity.ApplicantsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantsSnapshotRepository extends JpaRepository<ApplicantsSnapshot, Long> {

    Optional<ApplicantsSnapshot> findBySessionId(Long sessionId);

    Optional<ApplicantsSnapshot> findById(Long id);

}
