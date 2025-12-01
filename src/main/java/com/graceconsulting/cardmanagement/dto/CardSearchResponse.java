package com.graceconsulting.cardmanagement.dto;

import java.util.UUID;

public record CardSearchResponse(
    boolean found,
    UUID id
) {
    public static CardSearchResponse notFound() {
        return new CardSearchResponse(false, null);
    }

    public static CardSearchResponse found(UUID id) {
        return new CardSearchResponse(true, id);
    }
}
