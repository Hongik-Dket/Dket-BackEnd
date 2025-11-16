package com.example.demo.domain.lottery.repository;

import com.example.demo.domain.lottery.entity.WinnersAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WinnersAggregateRepository extends JpaRepository<WinnersAggregate, Long> {

    Optional<WinnersAggregate> findBySessionId(Long sessionId);

}
