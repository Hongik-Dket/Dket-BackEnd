package com.example.demo.domain.concert.repository;

import com.example.demo.domain.concert.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findById(Long id);

    Optional<Session> findByIdAndConcertId(Long id, Long concertId);

    List<Session> findByIsBuyableTrueAndIsDrawnTrueAndMetadataUploadedTrue();
}
