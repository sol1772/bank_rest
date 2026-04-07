package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.NaturalId;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Random;

@Entity
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@RequiredArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "Cards")
public class Card {
    private static final Random RANDOMIZER = new Random();

    @NaturalId
    @Column(nullable = false, unique = true)
    private final String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private final User holder;

    @Column(nullable = false)
    private final YearMonth expiry;

    @Column(nullable = false)
    private String cvc;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private CardStatus status;

    private BigDecimal balance;

    @XmlTransient
    public void setCvc() {
        this.cvc = String.valueOf(nextInt());
    }

    private int nextInt() {
        return (int) ((RANDOMIZER.nextDouble() * 899) + 100);
    }

    @Override
    public String toString() {
        return "Card{" +
                "number='" + number + '\'' +
                ", holder=" + holder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(id, card.id) && Objects.equals(number, card.number) && Objects.equals(holder, card.holder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, holder);
    }
}
