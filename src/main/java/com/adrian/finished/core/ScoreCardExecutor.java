package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.*;

/**
 * AbilityExecutor for AbilitySpec.SCORE_CARD.
 *
 * Rules (see rule-files/game-abilities.json and AbilitySpec.SCORE_CARD):
 * - If Present contains the next number after the highest in Finished (1 if empty),
 *   move that card from Present to the end of Finished.
 * - After scoring, draw exactly one replacement card from the top of the DrawStack (if available)
 *   and append it to the end (rightmost) of Present.
 * - Applies for cards 1..47. Only one scoring per execution.
 */
public final class ScoreCardExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.SCORE_CARD) {
            throw new IllegalArgumentException("ScoreCardExecutor can only execute SCORE_CARD ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();

        // Determine next target number to score
        List<Card> finished = s.finishedPile().cards();
        int lastFinished = finished.isEmpty() ? 0 : finished.getLast().number();
        int next = lastFinished + 1;

        // Only cards 1..47 can be scored here
        if (next < 1 || next > 47) {
            return s; // nothing to do (either already beyond 47 or invalid)
        }

        List<Card> presentCards = s.present().cards();
        int idx = -1;
        for (int i = 0; i < presentCards.size(); i++) {
            if (presentCards.get(i).number() == next) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            return s; // next number not in present, no changes
        }

        // Build new Present: remove the scored card, preserve order for others
        List<Card> newPresentList = new ArrayList<>(presentCards.size());
        for (int i = 0; i < presentCards.size(); i++) {
            if (i != idx) newPresentList.add(presentCards.get(i));
        }

        // Build new Finished: append the scored card
        List<Card> newFinishedList = new ArrayList<>(finished.size() + 1);
        newFinishedList.addAll(finished);
        newFinishedList.add(presentCards.get(idx));

        // Draw one replacement: from draw stack if available, otherwise from the past area (oldest card)
        Deque<Card> drawSource = new ArrayDeque<>(s.drawStack().cards());
        Deque<Card> pastSource = new ArrayDeque<>(s.past().cards());
        Card replacement = null;
        if (!drawSource.isEmpty()) {
            replacement = drawSource.removeFirst();
        } else if (!pastSource.isEmpty()) {
            Card fromPast = pastSource.removeFirst();
            // Mark as not from draw stack when retrieved from past
            replacement = new Card(fromPast.number(), fromPast.abilitiesTriggered(), fromPast.maxAbilities(), false);
        }
        if (replacement != null) {
            newPresentList.add(replacement);
        }

        return new GameState(
                s.activeStash(),
                s.reservedStash(),
                new DrawStack(drawSource),
                new PresentArea(List.copyOf(newPresentList)),
                new PastArea(pastSource),
                s.futureAreas(),
                new FinishedPile(List.copyOf(newFinishedList)),
                s.activeAllCardsInFutureAreas()
        );
    }
}
