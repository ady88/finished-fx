package com.adrian.finished.model;

import com.adrian.finished.model.Card;

import java.util.List;
import java.util.Objects;

/**
 * Future Area — holds cards that will be brought back to Present at a later turn start.
 */
public record FutureArea(List<Card> cards) {
    public FutureArea {
        Objects.requireNonNull(cards, "cards");
        if (cards.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("cards cannot contain nulls");
        }
    }
}