package com.example.demo.domain.resale.entity;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.base.BaseEntity;
import com.example.demo.global.base.Constants;
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
@Table(
        indexes = {
                @Index(name = "idx_resale_session_status", columnList = "session_id, resale_status"),
                @Index(name = "idx_resale_ticket_status", columnList = "ticket_id, resale_status")
        }
)
public class Resale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resale_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private User buyer;

    private int priceKrw;
    private BigInteger priceWei;

    @Enumerated (EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private ResaleStatus resaleStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserved_by")
    private User reservedBy;

    private LocalDateTime reservationExpiresAt;

    private String txHash;

    public void setTxHash(String txHash) { this.txHash = txHash; }

    public void setReservation(User user) {
        this.resaleStatus = ResaleStatus.RESERVED;
        this.reservedBy = user;
        this.reservationExpiresAt = LocalDateTime.now().plusMinutes(Constants.RESALE_RESERVATION_EXPIRATION_MINUTES);
    }

    public void cancelReservation() {
        if (this.resaleStatus == ResaleStatus.RESERVED) {
            this.resaleStatus = ResaleStatus.AVAILABLE;
        }
        this.reservedBy = null;
        this.reservationExpiresAt = null;
    }

    public void sell() {
        this.resaleStatus = ResaleStatus.SOLD;
        this.buyer = this.reservedBy;
    }

}
