package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.PassportIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PassportInfoRepository extends JpaRepository<PassportIdentity, Long> {
    boolean existsByPassportNumber(String passportNumber);

    Optional<PassportIdentity> findByUserId(Long userId);
}
