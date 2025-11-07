package com.example.demo.global.zkp.repository;

import com.example.demo.global.zkp.entity.ZkRootVersion;
import com.example.demo.global.zkp.enums.ZkRootType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZkRootVersionRepository extends JpaRepository<ZkRootVersion, Long> {

    Optional<ZkRootVersion> findTopBySessionIdAndRootTypeOrderByCreatedAtDesc(Long sessionId, ZkRootType rootType);

}
