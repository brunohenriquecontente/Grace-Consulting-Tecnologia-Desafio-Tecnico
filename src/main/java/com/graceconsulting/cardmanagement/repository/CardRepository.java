package com.graceconsulting.cardmanagement.repository;

import com.graceconsulting.cardmanagement.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardNumberHash(String cardNumberHash);

    boolean existsByCardNumberHash(String cardNumberHash);
}
