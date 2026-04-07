package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserById(@Param("id") Long id);

    User findByUsername(@Param("username") String username);

    Page<User> findByUsernameLike(String username, Pageable pageable);
}
