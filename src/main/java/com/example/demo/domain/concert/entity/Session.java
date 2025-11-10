package com.example.demo.domain.concert.entity;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    private LocalDate date;

    @Builder.Default
    private boolean isDrawn = false;
    @Builder.Default
    private boolean isMinted = false;
    @Builder.Default
    private boolean isBuyable = false;

    private String entryCode;

    @Column(columnDefinition = "BINARY(32)")
    private byte[] winnersRoot;
    @Column(columnDefinition = "BINARY(32)")
    private byte[] ownersRoot;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Apply> applyList = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Ticket> ticketList = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Metadata> metadataList = new ArrayList<>();

    public void setIsDrawn() { this.isDrawn = true; }
    public void setIsDrawn(byte[] root) {
        this.winnersRoot = root;
        this.isDrawn = true;
    }
    public void setIsMinted() { this.isMinted = true; }
    public void setIsBuyable(boolean isBuyable) { this.isBuyable = isBuyable; }
    public void setEntryCode(String entryCode) { this.entryCode = entryCode; }

    public void addApply(Apply apply) { this.applyList.add(apply); }
    public void addTicket(Ticket ticket) { this.ticketList.add(ticket); }
    public void addMetadata(Metadata metadata) { this.metadataList.add(metadata); }

    public int getPaidCount() {
        return (int) this.ticketList.stream()
                .filter(ticket -> ticket.getPaidAt() != null)
                .count();
    }

}
