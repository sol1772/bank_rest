package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBlockingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardBlockingRequestRepository extends JpaRepository<CardBlockingRequest, Long> {
}
