package com.example.demo.domain.ownership.entity;

import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.user.entity.User;
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
        name = "ownership",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_aggregate_ord",
                columnNames = {"owners_aggregate_id", "ord_index"}
        )
)
public class Ownership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ownership_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owners_aggregate_id")
    private OwnersAggregate ownersAggregate;

    @Column(length = 66)
    private String leafHex;

    private int ordIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_id")
    private User user;

    @Builder.Default
    private boolean isActive = true;

    public void deactivate() { this.isActive = false; }

}
