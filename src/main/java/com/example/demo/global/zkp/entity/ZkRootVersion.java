package com.example.demo.global.zkp.entity;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.global.base.BaseEntity;
import com.example.demo.global.zkp.enums.ZkRootType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "zk_root_versions",
        indexes = {
                @Index(name = "idx_zk_root_versions_session", columnList = "session_id"),
                @Index(name = "idx_zk_root_versions_created", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_root_version_srt",
                        columnNames = {"session_id", "root_type", "created_at"})
        })
public class ZkRootVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zk_root_version_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ZkRootType rootType;

    @Column(columnDefinition = "BINARY(32)")
    private byte[] root;

}
