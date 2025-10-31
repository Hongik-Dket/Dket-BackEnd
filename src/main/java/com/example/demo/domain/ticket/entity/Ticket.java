package com.example.demo.domain.ticket.entity;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.global.base.BaseEntity;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "metadata_id")
    private Metadata metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    private LocalDateTime paidAt;

    private BigInteger tokenId;

    private LocalDateTime enteredAt;

    public void paidBy(User user) {
        this.paidAt = LocalDateTime.now();
        this.user = user;
        user.addTicket(this);
    }

    public void resellTo(User buyer) {
        this.getUser().removeTicket(this);

        this.user = buyer;
        buyer.addTicket(this);
    }

    public void enter() {this.enteredAt = LocalDateTime.now();}

}
