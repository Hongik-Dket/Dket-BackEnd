package com.example.demo.domain.event.entity;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    private LocalDate date;

    private String contractAddress;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<Apply> applyList;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<Ticket> ticketList;
}
