package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardBlockingRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "card_blocking_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardBlockingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_Id")
    private Card card;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private CardBlockingRequestStatus requestStatus = CardBlockingRequestStatus.PROCESSING;

    public CardBlockingRequest(Card card, CardBlockingRequestStatus requestStatus) {
        this.card = card;
        this.requestStatus = requestStatus;
    }
}
