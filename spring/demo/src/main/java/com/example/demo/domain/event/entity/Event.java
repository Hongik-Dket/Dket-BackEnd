package com.example.demo.domain.event.entity;

import com.example.demo.domain.photocard.entity.PhotoCard;
import com.example.demo.domain.event.enums.AgeLimit;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.global.base.BaseEntity;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @Column(length = 50)
    private String title;

    @Enumerated (EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(10)")
    private AgeLimit ageLimit;

    @Column(length = 100)
    private String location;

    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private int price;

    private int capacity;

    @Enumerated (EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private EventStatus eventStatus;

    private LocalDateTime applyStart;
    private LocalDateTime applyEnd;

    private String bannerUrl;

    private String posterUrl;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Session> sessions;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<PhotoCard> photoCards;

    public void addSession(Session session) { this.sessions.add(session); }
    public void addPhotoCard(PhotoCard photoCard) { this.photoCards.add(photoCard); }

    public void setEventStatus(EventStatus eventStatus) { this.eventStatus = eventStatus; }

}
