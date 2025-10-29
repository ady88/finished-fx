package com.adrian.finished.model;

import java.util.Deque;
import java.util.Objects;

/**
 * Draw Stack — a deque where the front is the top to draw from.
 * Operations expected by the rules:
 * - removeFirst(): draw from the top (beginning)
 * - addLast(): put cards under the stack (end of turn from Past)
 * - addFirst(): occasionally place a card on top (rare effects)
 *
 * Immutable record; the contained deque should be treated as unmodifiable by convention.
 */
public record DrawStack(Deque<Card> cards) {
    public DrawStack {
        Objects.requireNonNull(cards, "cards");
        if (cards.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("cards cannot contain nulls");
        }
    }
}