package com.example.demo.domain.apply.entity;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.global.base.BaseEntity;
import com.example.demo.domain.user.entity.User;
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
public class Apply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apply_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Enumerated (EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15)")
    private ApplyStatus applyStatus;

    public void setApplyStatus(ApplyStatus applyStatus) { this.applyStatus = applyStatus; }
}
