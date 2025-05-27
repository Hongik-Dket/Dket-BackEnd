package com.example.demo.domain.event.repository;

import com.example.demo.domain.event.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findById(Long id);

    List<Session> findByEventId(Long eventId);
}
