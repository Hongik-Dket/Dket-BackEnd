package com.example.demo.domain.lottery.entity;

import com.example.demo.domain.apply.entity.Apply;
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
@Table(
        name = "applicants_snapshot_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_snapshot_ord",
                columnNames = {"applicants_snapshot_id", "ord_index"}
        )
)
public class ApplicantsSnapshotItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicants_snapshot_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicants_snapshot_id")
    private ApplicantsSnapshot applicantsSnapshot;

    private int ordIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apply_id")
    private Apply apply;
}
