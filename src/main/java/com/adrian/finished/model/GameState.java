package com.adrian.finished.model;

import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of the entire game state, composed of the distinct areas and stashes.
 */
public record GameState(
        Stash activeStash,
        Stash reservedStash,
        DrawStack drawStack,
        PresentArea present,
        PastArea past,
        List<FutureArea> futureAreas,
        FinishedPile finishedPile,
        int activeAllCardsInFutureAreas,
        boolean gameEnd
) {
    public GameState {
        Objects.requireNonNull(activeStash, "activeStash");
        Objects.requireNonNull(reservedStash, "reservedStash");
        Objects.requireNonNull(drawStack, "drawStack");
        Objects.requireNonNull(present, "present");
        Objects.requireNonNull(past, "past");
        Objects.requireNonNull(futureAreas, "futureAreas");
        Objects.requireNonNull(finishedPile, "finishedPile");
        if (futureAreas.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("futureAreas cannot contain nulls");
        }
        if (activeAllCardsInFutureAreas < 0) {
            throw new IllegalArgumentException("activeAllCardsInFutureAreas cannot be negative");
        }
    }

    /**
     * Convenience constructor for existing code that doesn't specify gameEnd flag.
     * Defaults gameEnd to false.
     */
    public GameState(
            Stash activeStash,
            Stash reservedStash,
            DrawStack drawStack,
            PresentArea present,
            PastArea past,
            List<FutureArea> futureAreas,
            FinishedPile finishedPile,
            int activeAllCardsInFutureAreas
    ) {
        this(activeStash, reservedStash, drawStack, present, past, futureAreas, finishedPile, activeAllCardsInFutureAreas, false);
    }
}