package com.example.demo.domain.metadata.entity;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metadata extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metadata_id")
    private Long id;

    private String cid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(length = 20, unique = true)
    private String ticketNumber;

    @Column(length = 6)
    private String seatCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_card_id")
    private PhotoCard photoCard;

    public void setCid(String cid) { this.cid = cid; }

}
