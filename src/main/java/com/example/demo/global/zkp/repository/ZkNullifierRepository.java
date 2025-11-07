package com.example.demo.global.zkp.repository;

import com.example.demo.global.zkp.entity.ZkNullifier;
import com.example.demo.global.zkp.enums.ZkNullifierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZkNullifierRepository extends JpaRepository<ZkNullifier, Long> {

    @Query("""
        select n from ZkNullifier n
         where n.session.id = :sessionId
           and n.type = :type
           and n.nullifier = :nullifier
    """)
    Optional<ZkNullifier> findOne(Long sessionId, ZkNullifierType type, byte[] nullifier);

    boolean existsBySessionIdAndTypeAndNullifier(Long sessionId, ZkNullifierType type, byte[] nullifier);

}
