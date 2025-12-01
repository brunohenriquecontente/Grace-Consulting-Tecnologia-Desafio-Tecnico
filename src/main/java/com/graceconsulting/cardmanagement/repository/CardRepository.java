package com.graceconsulting.cardmanagement.repository;

import com.graceconsulting.cardmanagement.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByCardNumberHash(String cardNumberHash);

    boolean existsByCardNumberHash(String cardNumberHash);
}
