package com.example.demo.domain.event.entity;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.global.base.BaseEntity;
import com.example.demo.global.base.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private Boolean metadataUploaded;

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
    public void setMetadataUploaded() { this.metadataUploaded = true; }

    public void addMetadata(Metadata metadata) { this.metadataList.add(metadata); }
    public void addTicket(Ticket ticket) { this.ticketList.add(ticket); }

    public boolean isBuyableNow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStartDateTime = LocalDateTime.of(this.date, getEvent().getStartTime());

        return now.isBefore(eventStartDateTime.minusHours(Constants.PAYMENT_AVAILABLE_BEFORE_EVENT_START));
    }
}
