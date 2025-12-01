package com.graceconsulting.cardmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards", indexes = {
    @Index(name = "idx_card_number_hash", columnList = "cardNumberHash")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "card_number_encrypted", nullable = false, length = 512)
    private String cardNumberEncrypted;

    @Column(name = "card_number_hash", nullable = false, unique = true, length = 64)
    private String cardNumberHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "batch_id")
    private String batchId;
}
