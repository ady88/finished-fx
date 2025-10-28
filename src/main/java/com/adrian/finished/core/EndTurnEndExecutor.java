package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.*;

/**
 * AbilityExecutor for AbilitySpec.END_TURN_END.
 *
 * Rules implemented (see game-abilities.json and AbilitySpec.END_TURN_END):
 * - If there are more than 3 cards in the past area, remove the oldest cards one by one
 *   and place them under the draw stack (at the end), preserving their order.
 * - If there are 3 or fewer cards in the past area, do nothing.
 */
public final class EndTurnEndExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.END_TURN_END) {
            throw new IllegalArgumentException("EndTurnEndExecutor can only execute END_TURN_END ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();

        Deque<Card> past = new ArrayDeque<>(s.past().cards());
        if (past.size() <= 3) {
            return s; // nothing to do
        }

        Deque<Card> draw = new ArrayDeque<>(s.drawStack().cards());
        // Move oldest cards (from front) to the bottom of draw until only 3 remain in past
        List<Card> moved = new ArrayList<>();
        while (past.size() > 3) {
            Card c = past.removeFirst();
            moved.add(c);
        }
        // Preserve relative order when appending under draw
        for (Card c : moved) {
            draw.addLast(c);
        }

        return new GameState(
                s.activeStash(),
                s.reservedStash(),
                new DrawStack(draw),
                new PresentArea(s.present().cards()),
                new PastArea(past),
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}
