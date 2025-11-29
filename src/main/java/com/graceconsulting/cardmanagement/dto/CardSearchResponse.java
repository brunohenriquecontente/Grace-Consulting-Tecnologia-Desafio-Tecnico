package com.graceconsulting.cardmanagement.dto;

public record CardSearchResponse(
    boolean found,
    Long id
) {
    public static CardSearchResponse notFound() {
        return new CardSearchResponse(false, null);
    }

    public static CardSearchResponse found(Long id) {
        return new CardSearchResponse(true, id);
    }
}
