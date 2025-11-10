package com.example.demo.domain.lottery.entity;

import com.example.demo.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WinnersEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "winners_event_id")
    private Long id;

    private Long sessionId;
    private Integer accepted;
    private Long blockNumber;

    @Column(length = 66, nullable = false)
    private String txHash;

    private Integer logIndex;

    @ElementCollection
    @CollectionTable(name = "winners_event_indices",
            joinColumns = @JoinColumn(name = "winners_event_id"))
    @Column(name = "winner_index")
    private List<Integer> winnerIndices;
}
