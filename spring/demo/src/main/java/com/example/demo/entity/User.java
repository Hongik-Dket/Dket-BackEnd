package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Event> events;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Apply> applies;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Ticket> tickets;

    public void addEvent(Event event) { this.events.add(event); }
    public void addApply(Apply apply) { this.applies.add(apply); }
    public void addTicket(Ticket ticket) { this.tickets.add(ticket); }

}