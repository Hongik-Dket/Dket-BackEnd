package com.example.demo.domain.concert.entity;

import com.example.demo.domain.metadata.entity.PhotoCard;
import com.example.demo.domain.concert.enums.AgeLimit;
import com.example.demo.domain.concert.enums.ConcertStatus;
import com.example.demo.global.base.BaseEntity;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Concert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concert_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    private String title;

    @Enumerated (EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(10)")
    private AgeLimit ageLimit;

    private String location;

    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private int priceKrw;
    private BigInteger priceWei;

    private int capacity;

    @Enumerated (EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private ConcertStatus concertStatus;

    private LocalDateTime applyStart;
    private LocalDateTime applyEnd;

    private String bannerUrl;
    private String posterUrl;

    private String txHash;

    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Session> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PhotoCard> photoCards = new ArrayList<>();

    public void addSession(Session session) { this.sessions.add(session); }
    public void addPhotoCard(PhotoCard photoCard) { this.photoCards.add(photoCard); }

    public void setConcertStatus(ConcertStatus concertStatus) { this.concertStatus = concertStatus; }
    public void setTxHash(String txHash) { this.txHash = txHash; }

}
