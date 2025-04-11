package com.example.demo.domain.ticket.entity;

import com.example.demo.domain.photocard.entity.PhotoCard;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(length = 20, unique = true)
    private String ticketNumber;

    private LocalDateTime paidAt;

    @Pattern(regexp = "\\d{6}", message = "좌석 코드는 정확히 6자리 숫자여야 합니다.")
    @Column(length = 6)
    private String seatCode;

    private String tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_card_id")
    private PhotoCard photoCard;

    private LocalDateTime enteredAt;

    public void setSeatCode(int seatNumber) {
        if (seatNumber < 0 || seatNumber > 999999)
            throw new CustomException(ErrorStatus.TICKET_INVALID_SEAT);

        this.seatCode = String.format("%06d", seatNumber);
    }

}
