package com.example.demo.domain.metadata.repository;

import com.example.demo.domain.metadata.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetadataRepository extends JpaRepository<Metadata, Long> {

    Optional<Metadata> findById(Long id);
}
