package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.*;

/**
 * AbilityExecutor for AbilitySpec.BEGIN_TURN.
 *
 * Rules implemented (see rule-files/game-abilities.json and AbilitySpec.BEGIN_TURN):
 * - If activeAllCardsInFutureAreas == 0: draw up to 3 cards from the draw stack into a new Present area.
 *   Existing Present is replaced (not appended).
 * - If activeAllCardsInFutureAreas > 0: take cards from the first available FutureArea (index 0)
 *   into the Present area, remove that FutureArea from the list, and decrement the counter by 1.
 *
 * Notes:
 * - If there are fewer than 3 cards in the draw stack, draw as many as available (0..2).
 * - If the counter > 0 but there is no future area available (defensive), fallback to drawing from the stack.
 */
public final class BeginTurnExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        System.out.println("ADRIAN 2");
        if (context.ability() != AbilitySpec.BEGIN_TURN) {
            throw new IllegalArgumentException("BeginTurnExecutor can only execute BEGIN_TURN ability");
        }
        System.out.println("ADRIAN 3");
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();

        System.out.println("ADRIAN 3");
        int counter = s.activeAllCardsInFutureAreas();
        System.out.println("ADRIAN 3");
        PresentArea newPresent;
        List<FutureArea> newFutures;
        int newCounter;
        DrawStack newDrawStack = s.drawStack();

        if (counter > 0 && !s.futureAreas().isEmpty()) {
            // Resolve the first future area into present
            FutureArea first = s.futureAreas().getFirst();
            newPresent = new PresentArea(first.cards());
            // Remove the first future area
            newFutures = List.copyOf(s.futureAreas().subList(1, s.futureAreas().size()));
            newCounter = counter - 1;
        } else {
            System.out.println("ADRIAN 4");
            System.out.println(s.drawStack().cards().size());
            // Draw up to 3 cards from the draw stack
            Deque<Card> source = new ArrayDeque<>(s.drawStack().cards());
            List<Card> drawn = new ArrayList<>(3);
            for (int i = 0; i < 3 && !source.isEmpty(); i++) {
                drawn.add(source.removeFirst());
            }
            newPresent = new PresentArea(List.copyOf(drawn));
            newDrawStack = new DrawStack(source);
            newFutures = s.futureAreas();
            newCounter = counter; // unchanged
        }

        return new GameState(
                s.activeStash(),
                s.reservedStash(),
                newDrawStack,
                newPresent,
                s.past(),
                newFutures,
                s.finishedPile(),
                newCounter
        );
    }
}
