package com.example.demo.domain.photocard.entity;

import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_card_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(length = 500)
    private String ipfsUrl;

    @OneToMany(mappedBy = "photoCard")
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    public void addTicket(Ticket ticket) { this.tickets.add(ticket); }
}
