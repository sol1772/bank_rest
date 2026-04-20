package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByNumber(String number);

    Optional<Card> findByIdAndHolder_Username(Long id, String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdForUpdate(Long id);

    Page<Card> findAllByHolder_Username(String username, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.balance), 0) FROM Card c WHERE c.holder.username = :username")
    BigDecimal getUserBalance(@Param("username") String username);
}
