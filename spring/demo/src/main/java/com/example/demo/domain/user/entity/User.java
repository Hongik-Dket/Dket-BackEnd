package com.example.demo.domain.user.entity;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    private int age;

    private String walletAddress;

    private LocalDateTime withdrawTime;

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Event> organizedEvents = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Apply> applies =  new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    public void addEvent(Event event) { this.organizedEvents.add(event); }
    public void addApply(Apply apply) { this.applies.add(apply); }
    public void addTicket(Ticket ticket) { this.tickets.add(ticket); }

}