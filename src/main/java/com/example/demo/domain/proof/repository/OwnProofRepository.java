package com.example.demo.domain.proof.repository;

import com.example.demo.domain.proof.entity.OwnProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnProofRepository extends JpaRepository<OwnProof, Long> {

    Optional<OwnProof> findById(String id);

    boolean existsByTicketId(Long ticketId);

    Optional<OwnProof> findByTicketId(Long ticketId);
}
