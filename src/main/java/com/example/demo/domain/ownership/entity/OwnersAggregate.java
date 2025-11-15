package com.example.demo.domain.ownership.entity;

import com.example.demo.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnersAggregate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "owners_aggregate_id")
    private Long id;

    @Column(unique = true)
    private Long sessionId;

    @Builder.Default
    private Integer ownersCount = 0;

    @Column(length = 66)
    private String poseidonMerkleRoot;

    private Long updatedBlockNumber;

    @OneToMany(mappedBy = "ownersAggregate", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Ownership> items = new ArrayList<>();

    public void addItem(Ownership ownership) {
        this.items.add(ownership);
        this.ownersCount = this.items.size();
    }

    public void update(Integer count, String poseidonMerkleRoot, Long updatedBlockNumber) {
        this.ownersCount = count;
        this.poseidonMerkleRoot = poseidonMerkleRoot;
        this.updatedBlockNumber = updatedBlockNumber;
    }
}
