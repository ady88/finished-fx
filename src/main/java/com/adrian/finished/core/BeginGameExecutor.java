package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.*;

/**
 * AbilityExecutor for AbilitySpec.BEGIN_GAME.
 *
 * Rules implemented (see rule-files/game-abilities.json and game-info.md):
 * - Reset stashes to game start: active stash has 5 candy and 7 coffee; reserved stash has 5 candy and 0 coffee.
 * - Move all cards from every area into the draw stack and (re)build it as:
 *   shuffled cards 1..47 on top, then card 48 at the bottom. No other cards remain in any area.
 * - Clear Present, Past, Future areas and Finished pile.
 * - Reset activeAllCardsInFutureAreas to 0.
 *
 * Notes:
 * - The shuffle can be injected with a java.util.Random for deterministic testing.
 */
public final class BeginGameExecutor implements AbilityExecutor {
    private final Random random;

    /**
     * Creates an executor with a default Random.
     */
    public BeginGameExecutor() {
        this(new Random());
    }

    /**
     * Creates an executor using the provided Random (useful for deterministic tests).
     */
    public BeginGameExecutor(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.BEGIN_GAME) {
            throw new IllegalArgumentException("BeginGameExecutor can only execute BEGIN_GAME ability");
        }

        // Build shuffled deck: 1..47 shuffled, then 48 at bottom
        List<Card> top47 = new ArrayList<>(47);
        for (int i = 1; i <= 47; i++) {
            top47.add(new Card(i, 0, getMaxAbilitiesForCard(i)));
        }
        Collections.shuffle(top47, random);
        Deque<Card> deque = new ArrayDeque<>(top47);
        deque.addLast(new Card(48, 0, getMaxAbilitiesForCard(48))); // Card 48 is always the bottom card

        // Construct empty areas and piles
        PresentArea emptyPresent = new PresentArea(List.of());
        PastArea emptyPast = new PastArea(new ArrayDeque<>());
        List<FutureArea> noFutures = List.of();
        FinishedPile emptyFinished = new FinishedPile(List.of());

        // Stashes per game start rules
        Stash active = new Stash(5, 7);
        Stash reserved = new Stash(5, 0);

        return new GameState(
                active,
                reserved,
                new DrawStack(deque),
                emptyPresent,
                emptyPast,
                noFutures,
                emptyFinished,
                0
        );
    }

    /**
     * Determines the maximum abilities for a given card number based on AbilitySpec definitions.
     * Only counts abilities that require candy (manual abilities).
     */
    private static int getMaxAbilitiesForCard(int cardNumber) {
        // Check all ability specs to see if this card provides any candy-requiring abilities
        for (AbilitySpec spec : AbilitySpec.values()) {
            if (spec.cards().contains(cardNumber) && spec.requiresCandy()) {
                return spec.limit();
            }
        }
        return 0; // Card has no manual (candy-requiring) abilities
    }
}