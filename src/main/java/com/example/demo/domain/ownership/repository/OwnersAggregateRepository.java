package com.example.demo.domain.ownership.repository;

import com.example.demo.domain.ownership.entity.OwnersAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnersAggregateRepository extends JpaRepository<OwnersAggregate, Long> {

    Optional<OwnersAggregate> findBySessionId(Long sessionId);

}
