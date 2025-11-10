package com.example.demo.domain.lottery.entity;

import com.example.demo.global.base.BaseEntity;
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
public class WinnersAggregate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "winners_aggregate_id")
    private Long id;

    @Column(unique = true)
    private Long sessionId;
    private Integer winnersCount;

    @Column(length = 66)
    private String poseidonMerkleRoot;

    private Long updatedBlockNumber;

    public void update(Integer winnersCount, String poseidonMerkleRoot, Long updatedBlockNumber) {
        this.winnersCount = winnersCount;
        this.poseidonMerkleRoot = poseidonMerkleRoot;
        this.updatedBlockNumber = updatedBlockNumber;
    }
}
