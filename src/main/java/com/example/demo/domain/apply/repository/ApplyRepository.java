package com.example.demo.domain.apply.repository;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Apply a SET a.applyStatus = :newStatus " +
            "WHERE a.session.id = :sessionId AND a.applyStatus = :currentStatus")
    int batchUpdateApplyStatusBySessionId(@Param("sessionId") Long sessionId,
                                          @Param("currentStatus") ApplyStatus currentStatus,
                                          @Param("newStatus") ApplyStatus newStatus);

    @Query("SELECT a.user.walletAddress FROM Apply a WHERE a.session.id = :sessionId")
    List<String> findWalletAddressesBySessionId(@Param("sessionId") Long sessionId);

    @Modifying
    @Query("UPDATE Apply a SET a.applyStatus = :newStatus " +
            "WHERE a.session.id = :sessionId AND a.user.walletAddress IN :walletAddresses")
    int batchUpdateApplyStatusBySessionIdAndWalletAddresses(
            @Param("sessionId") Long sessionId,
            @Param("walletAddresses") List<String> walletAddresses,
            @Param("newStatus") ApplyStatus newStatus
    );

    @Modifying
    @Query("UPDATE Apply a SET a.applyStatus = :newStatus " +
            "WHERE a.session.id = :sessionId AND a.user.walletAddress NOT IN :walletAddresses")
    int batchUpdateStatusExceptWallets(
            @Param("sessionId") Long sessionId,
            @Param("walletAddresses") List<String> walletAddresses,
            @Param("newStatus") ApplyStatus newStatus
    );



}
