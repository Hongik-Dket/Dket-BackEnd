package com.example.demo.domain.ownership.entity;

import com.example.demo.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnersEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owners_event_id")
    private Long id;

    private Long sessionId;
    private Long blockNumber;

    @Column(length = 66, nullable = false)
    private String txHash;

    private Integer logIndex;

    private String ownerAddress;
    private BigInteger tokenId;
}
