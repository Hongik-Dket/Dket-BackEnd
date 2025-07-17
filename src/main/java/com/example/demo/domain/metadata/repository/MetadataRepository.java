package com.example.demo.domain.metadata.repository;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.metadata.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MetadataRepository extends JpaRepository<Metadata, Long> {

    Optional<Metadata> findById(Long id);

    @Query("SELECT m.session FROM Metadata m WHERE m.id = :metadataId")
    Session findSessionByMetadataId(@Param("metadataId") Long metadataId);

    List<Metadata> findAllByCidIn(List<String> cids);

    Optional<Metadata> findByTicketNumber(String ticketNumber);
}
