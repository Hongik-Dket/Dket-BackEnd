package com.example.demo.domain.ownership.repository;

import com.example.demo.domain.ownership.entity.OwnersEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnersEventRepository  extends JpaRepository<OwnersEvent, Long> {

    boolean existsByTxHashAndLogIndex(String txHash, Integer logIndex);

}
