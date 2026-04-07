package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface CardRepository extends JpaRepository<Card, Long> {
    Card findByNumber(@Param("number") String number);

    Card findByIdAndHolder_Username(Long id, String username);

    Page<Card> findAllByHolder_Username(String username, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(c.balance), 0) FROM Card c WHERE c.holder.username = :username")
    BigDecimal getUserBalance(@Param("holder") String username);
}
