package com.adrian.finished.model;

import java.util.List;
import java.util.Objects;

/**
 * Finished (Scored) Pile — ascending order of scored cards.
 */
public record FinishedPile(List<Card> cards) {
    public FinishedPile {
        Objects.requireNonNull(cards, "cards");
        if (cards.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("cards cannot contain nulls");
        }
    }
}