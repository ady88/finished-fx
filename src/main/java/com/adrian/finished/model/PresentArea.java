package com.adrian.finished.model;

import com.adrian.finished.model.Card;

import java.util.List;
import java.util.Objects;

/**
 * Present Area — where the player sorts cards and uses abilities during their turn.
 * Cards are ordered left-to-right as stored (index 0 is leftmost).
 */
public record PresentArea(List<Card> cards) {
    public PresentArea {
        Objects.requireNonNull(cards, "cards");
        if (cards.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("cards cannot contain nulls");
        }
    }
}