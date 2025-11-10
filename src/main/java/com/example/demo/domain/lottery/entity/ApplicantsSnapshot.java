package com.example.demo.domain.lottery.entity;

import com.example.demo.domain.concert.entity.Session;
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
public class ApplicantsSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicants_snapshot_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(length = 66)
    private String listHash;

    private int totalCount;

    @OneToMany(mappedBy = "applicantsSnapshot", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ApplicantsSnapshotItem> items = new ArrayList<>();

    public void addItem(ApplicantsSnapshotItem item) {
        this.items.add(item);
        this.totalCount = this.items.size();
    }

    public void finalize(String listHash, int totalCount) {
        this.listHash = listHash;
        this.totalCount = totalCount;
    }

}
