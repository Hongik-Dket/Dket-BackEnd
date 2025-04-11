package com.example.demo.apply.domain;

import com.example.demo.apply.enums.ApplyStatus;
import com.example.demo.event.domain.Session;
import com.example.demo.global.base.BaseEntity;
import com.example.demo.user.domain.User;
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

}
