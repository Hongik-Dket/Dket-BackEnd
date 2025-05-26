package com.example.demo.domain.apply.repository;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Apply a SET a.applyStatus = :newStatus WHERE a.applyStatus = :currentStatus")
    int batchUpdateApplyStatus(@Param("currentStatus") ApplyStatus currentStatus,
                               @Param("newStatus") ApplyStatus newStatus);

}
