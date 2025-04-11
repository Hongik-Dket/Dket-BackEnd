package com.example.demo.photocard.domain;

import com.example.demo.event.domain.Event;
import com.example.demo.ticket.domain.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private List<Ticket> tickets;

    public void addTicket(Ticket ticket) { this.tickets.add(ticket); }
}
