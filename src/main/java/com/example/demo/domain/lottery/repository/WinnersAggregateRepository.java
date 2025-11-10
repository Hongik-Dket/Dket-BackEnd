package com.example.demo.domain.lottery.repository;

import com.example.demo.domain.lottery.entity.WinnersAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WinnersAggregateRepository extends JpaRepository<WinnersAggregate, Long> {

    Optional<WinnersAggregate> findBySessionId(Long sessionId);

}
