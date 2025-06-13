package com.example.demo.domain.event.entity;

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
    @JoinColumn(name = "event_id")
    private Event event;

    private LocalDate date;

    private String txHash;

    private Boolean isDrawn;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Apply> applyList = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Ticket> ticketList = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Metadata> metadataList = new ArrayList<>();

    public void setTxHash(String txHash) { this.txHash = txHash; }
    public void setIsDrawn() { this.isDrawn = true; }

    public void addMetadata(Metadata metadata) { this.metadataList.add(metadata); }
}
