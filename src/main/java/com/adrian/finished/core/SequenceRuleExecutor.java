package com.adrian.finished.core;

import com.adrian.finished.model.GameState;
import com.adrian.finished.model.PastArea;
import com.adrian.finished.model.PresentArea;
import com.adrian.finished.model.Stash;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AbilityExecutor for AbilitySpec.SEQUENCE_RULE.
 *
 * Rules implemented (see rule-files/game-abilities.json and AbilitySpec.SEQUENCE_RULE):
 * - After moving Present to Past (END_TURN_BEGIN), detect ascending sequences of length >= 3
 *   within the cards that are now in the Past area (in their left-to-right order).
 * - For each such sequence, award (length - 1) candies by moving them from reserved stash to active stash.
 * - Multiple sequences may exist; total award is the sum across sequences, but cannot exceed available reserve.
 *
 * Note: The current model does not track exactly which Past cards were moved this turn. Tests for this executor
 *       provide a Past area containing only the just-moved cards. The logic here scans the Past cards in order
 *       and finds all maximal strictly increasing-by-1 runs and applies the rule to each run of length >= 3.
 */
public final class SequenceRuleExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.SEQUENCE_RULE) {
            throw new IllegalArgumentException("SequenceRuleExecutor can only execute SEQUENCE_RULE ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();
        List<Integer> pastNumbers = new ArrayList<>();
        s.past().cards().forEach(card -> pastNumbers.add(card.number()));

        int totalAward = computeTotalAward(pastNumbers);
        if (totalAward <= 0) {
            return s; // nothing to do
        }

        int delta = Math.min(totalAward, s.reservedStash().candy());
        if (delta == 0) {
            return s; // no reserve to transfer
        }

        Stash newActive = new Stash(s.activeStash().candy() + delta, s.activeStash().coffee());
        Stash newReserved = new Stash(s.reservedStash().candy() - delta, s.reservedStash().coffee());

        return new GameState(
                newActive,
                newReserved,
                s.drawStack(),
                new PresentArea(s.present().cards()),
                new PastArea(s.past().cards()),
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }

    static int computeTotalAward(List<Integer> numbersInOrder) {
        if (numbersInOrder == null || numbersInOrder.isEmpty()) return 0;
        int total = 0;
        int runLen = 1;
        for (int i = 1; i < numbersInOrder.size(); i++) {
            int prev = numbersInOrder.get(i - 1);
            int cur = numbersInOrder.get(i);
            if (cur == prev + 1) {
                runLen++;
            } else {
                if (runLen >= 3) total += (runLen - 1);
                runLen = 1;
            }
        }
        if (runLen >= 3) total += (runLen - 1);
        return total;
    }
}
