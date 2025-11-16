package com.example.demo.global.zkp.signature.entity;

import com.example.demo.global.base.BaseEntity;
import com.example.demo.global.zkp.signature.enums.ChallengePurpose;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Challenge extends BaseEntity {

    @Id
    private String id; // UUID

    private String message;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private ChallengePurpose purpose;

    private Long sessionId;
    private Long resaleId;

    private String nonceHex;

    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean used = false;

    public void setMessage(String message) { this.message = message; }
    public void setUsed() {  this.used = true; }
}
