package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Table(name = "Bank_transactions")
public class BankTransaction {
    private static final Logger logger = LoggerFactory.getLogger(BankTransaction.class);

    private final String transactionReference;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "from_Card_Id")
    private Long fromCardId;

    @JoinColumn(name = "to_Card_Id")
    private Long toCardId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public BankTransaction(String transactionReference, Long fromCardId, Long toCardId,
                           BigDecimal amount, TransactionStatus status) {
        this.transactionReference = transactionReference;
        this.fromCardId = fromCardId;
        this.toCardId = toCardId;
        this.amount = amount;
        this.status = status;
    }

    @Override
    public String toString() {
        return transactionReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankTransaction that = (BankTransaction) o;
        return Objects.equals(id, that.id) && Objects.equals(transactionReference, that.transactionReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionReference);
    }
}
