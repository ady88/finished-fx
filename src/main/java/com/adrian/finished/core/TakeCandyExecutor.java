package com.adrian.finished.core;

import com.adrian.finished.model.GameState;
import com.adrian.finished.model.PresentArea;
import com.adrian.finished.model.Stash;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * AbilityExecutor for AbilitySpec.TAKE_CANDY.
 *
 * Rules (see rule-files/game-abilities.json and AbilitySpec.TAKE_CANDY):
 * - When, at the start of a turn, cards enter the Present area from the draw stack,
 *   for each card that has the take-candy symbol (cards listed in AbilitySpec.TAKE_CANDY.cards()),
 *   gain 1 candy by moving it from the reserved stash to the active stash.
 * - This does NOT trigger if cards came from Past or Future areas.
 * - If the reserved stash has fewer candies than take-candy cards, only the available amount is transferred.
 *
 * Implementation note:
 * - Uses the Card.fromDrawStack() field to determine if a card originated from the draw stack.
 */
public final class TakeCandyExecutor implements AbilityExecutor {

    private static final Set<Integer> TAKE_CANDY_NUMBERS = new HashSet<>(AbilitySpec.TAKE_CANDY.cards());

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.TAKE_CANDY) {
            throw new IllegalArgumentException("TakeCandyExecutor can only execute TAKE_CANDY ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();

        // Count take-candy cards that came from draw stack
        long takeCandyCount = s.present().cards().stream()
                .filter(card -> card.fromDrawStack() && TAKE_CANDY_NUMBERS.contains(card.number()))
                .count();

        if (takeCandyCount <= 0) {
            return s; // nothing to do
        }

        int available = s.reservedStash().candy();
        int delta = (int) Math.min(takeCandyCount, available);
        if (delta == 0) {
            return s; // no reserve to transfer
        }

        // Move delta candies from reserved to active
        Stash newActive = new Stash(s.activeStash().candy() + delta, s.activeStash().coffee());
        Stash newReserved = new Stash(s.reservedStash().candy() - delta, s.reservedStash().coffee());

        return new GameState(
                newActive,
                newReserved,
                s.drawStack(),
                new PresentArea(s.present().cards()), // unchanged
                s.past(),
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}