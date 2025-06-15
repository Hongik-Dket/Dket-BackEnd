package com.example.demo.domain.metadata.repository;

import com.example.demo.domain.metadata.entity.PhotoCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoCardRepository extends JpaRepository<PhotoCard, Long> {

    Optional<PhotoCard> findById(Long photoCardId);

    List<PhotoCard> findAllByIdIn(List<Long> ids);
}
