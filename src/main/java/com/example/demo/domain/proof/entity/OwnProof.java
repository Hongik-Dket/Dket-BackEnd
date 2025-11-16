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

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnProof extends BaseEntity {

    @Id
    private String id;

    private Long sessionId;
    private Long ticketId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String proofJson;

    private String nullifier;

}
