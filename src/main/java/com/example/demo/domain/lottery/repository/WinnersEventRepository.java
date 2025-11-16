package com.example.demo.domain.lottery.repository;

import com.example.demo.domain.lottery.entity.WinnersEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WinnersEventRepository extends CrudRepository<WinnersEvent, Long> {

    boolean existsByTxHashAndLogIndex(String txHash, Integer logIndex);

}
