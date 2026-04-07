package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardBlockingRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Card_blocking_requests")
public class CardBlockingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    private Long cardId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private CardBlockingRequestStatus requestStatus = CardBlockingRequestStatus.PROCESSING;

    public CardBlockingRequest(Long cardId, CardBlockingRequestStatus requestStatus) {
        this.cardId = cardId;
        this.requestStatus = requestStatus;
    }
}
