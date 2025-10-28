package com.adrian.finished.model;

import com.adrian.finished.model.Card;

import java.util.Deque;
import java.util.Objects;

/**
 * Past Area — a queue of most recently moved cards from Present. The newest cards are typically added at the end.
 * By end of turn rules, only the 3 most recent cards remain; older ones cycle under the draw stack.
 */
public record PastArea(Deque<Card> cards) {
    public PastArea {
        Objects.requireNonNull(cards, "cards");
        if (cards.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("cards cannot contain nulls");
        }
    }
}