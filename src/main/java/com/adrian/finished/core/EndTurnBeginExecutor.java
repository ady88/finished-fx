

package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * AbilityExecutor for AbilitySpec.END_TURN_BEGIN.
 *
 * Rules implemented (see rule-files/game-abilities.json and AbilitySpec.END_TURN_BEGIN):
 * - First, move all existing cards from the Past area below the draw stack (if any).
 * - Then, move all cards from the Present area into the Past area, preserving their left-to-right order.
 * - Remove all candy from these moved cards and return the candy to the reserved stash.
 * - Reset abilitiesTriggered to 0 for all cards moved to past (representing candies being removed).
 */
public final class EndTurnBeginExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.END_TURN_BEGIN) {
            throw new IllegalArgumentException("EndTurnBeginExecutor can only execute END_TURN_BEGIN ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();

        // Step 1: Move all cards from Past area below the draw stack (if any)
        Deque<Card> newDrawStackDeque = new ArrayDeque<>(s.drawStack().cards());
        // Add all past cards below the draw stack (at the end)
        newDrawStackDeque.addAll(s.past().cards());
        DrawStack newDrawStack = new DrawStack(newDrawStackDeque);

        // Step 2: Calculate total candies to return from present cards (sum of abilitiesTriggered)
        int candiesToReturn = s.present().cards().stream()
                .mapToInt(Card::abilitiesTriggered)
                .sum();

        // Update reserved stash with returned candies
        Stash newReservedStash = new Stash(
                s.reservedStash().candy() + candiesToReturn,
                s.reservedStash().coffee()
        );

        // Step 3: Build new Past from present cards (past is now empty after step 1)
        // Reset abilitiesTriggered to 0 for all moved cards (candies removed)
        Deque<Card> newPastDeque = new ArrayDeque<>();
        for (Card c : s.present().cards()) {
            Card resetCard = new Card(c.number(), 0, c.maxAbilities(), c.fromDrawStack());
            newPastDeque.addLast(resetCard);
        }

        // New Present is empty
        PresentArea newPresent = new PresentArea(List.of());

        return new GameState(
                s.activeStash(),
                newReservedStash,
                newDrawStack,
                newPresent,
                new PastArea(newPastDeque),
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}