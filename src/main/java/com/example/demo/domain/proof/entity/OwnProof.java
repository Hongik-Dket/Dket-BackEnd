package com.example.demo.domain.proof.entity;

import com.example.demo.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
public class OwnProof extends BaseEntity {

    @Id
    private String id;

    private Long sessionId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String proofJson;

    private String root;

    private String nullifier;

}
