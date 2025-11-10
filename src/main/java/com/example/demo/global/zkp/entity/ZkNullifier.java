//package com.example.demo.global.zkp.entity;
//
//import com.example.demo.domain.concert.entity.Session;
//import com.example.demo.global.base.BaseEntity;
//import com.example.demo.global.zkp.enums.ZkNullifierType;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "zk_nullifiers",
//        indexes = {
//                @Index(name = "idx_nullifiers_session", columnList = "session_id"),
//                @Index(name = "idx_nullifiers_used_on_chain", columnList = "used_on_chain")
//        },
//        uniqueConstraints = {
//                // 동일 (세션, 타입, 널리파이어) 중복 사용 방지
//                @UniqueConstraint(name = "uk_nullifier_unique",
//                        columnNames = {"session_id", "nf_type", "nullifier"})
//        })
//public class ZkNullifier extends BaseEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "zk_nullifier_id")
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "session_id")
//    private Session session;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 16)
//    private ZkNullifierType type;
//
//    @Column(columnDefinition = "BINARY(32)")
//    private byte[] nullifier;
//
//    private boolean usedOnChain = false;
//
//    private LocalDateTime usedAt;
//
//    public void markUsed() {
//        this.usedOnChain = true;
//        this.usedAt = LocalDateTime.now();
//    }
//}
