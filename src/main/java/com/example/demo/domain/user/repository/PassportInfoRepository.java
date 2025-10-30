package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.PassportInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassportInfoRepository extends JpaRepository<PassportInfo, Long> {
    boolean existsByPassportNumber(String passportNumber);
}
