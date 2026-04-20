package com.example.bankcards.entity;

import com.example.bankcards.dto.mappers.YearMonthConverter;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.util.MaskingUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Column(nullable = false, unique = true)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User holder;

    @Convert(converter = YearMonthConverter.class)
    @Column(nullable = false, columnDefinition = "VARCHAR(7)")
    private YearMonth expiry;

    @Column(nullable = false, length = 255)
    private String cvc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false)
    private String iv;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(number, card.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", number='" + MaskingUtil.maskCardNumber(number) + '\'' +
                ", status=" + status +
                '}';
    }
}
