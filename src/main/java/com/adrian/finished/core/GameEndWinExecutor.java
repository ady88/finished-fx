
package com.adrian.finished.core;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.FinishedPile;
import com.adrian.finished.model.GameState;
import com.adrian.finished.model.PresentArea;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AbilityExecutor for AbilitySpec.GAME_END_WIN.
 *
 * Rule: Score card 48 and finalize the game. This executor only sets gameEnd = true
 * when card 48 can actually be scored from the present area (i.e., when card 48 is in present
 * and the finished pile ends with card 47). If conditions are not met, preserves the current gameEnd flag.
 */
public final class GameEndWinExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.GAME_END_WIN) {
            throw new IllegalArgumentException("GameEndWinExecutor can only execute GAME_END_WIN ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();

        // Check if card 48 can be scored: it must be in present area and finished pile must end with 47
        List<Card> finished = s.finishedPile().cards();
        int lastFinished = finished.isEmpty() ? 0 : finished.getLast().number();

        // Can only score 48 if the last finished card is 47
        if (lastFinished != 47) {
            // Cannot score 48 - return unchanged state
            return s;
        }

        // Find card 48 in present area
        List<Card> present = new ArrayList<>(s.present().cards());
        int idx48 = -1;
        for (int i = 0; i < present.size(); i++) {
            if (present.get(i).number() == 48) {
                idx48 = i;
                break;
            }
        }

        if (idx48 >= 0) {
            // Score card 48 by moving it from present to finished and set gameEnd = true
            Card card48 = present.remove(idx48);
            List<Card> newFinished = new ArrayList<>(finished);
            newFinished.add(card48);

            return new GameState(
                    s.activeStash(),
                    s.reservedStash(),
                    s.drawStack(),
                    new PresentArea(List.copyOf(present)),
                    s.past(),
                    s.futureAreas(),
                    new FinishedPile(List.copyOf(newFinished)),
                    s.activeAllCardsInFutureAreas(),
                    true
            );
        }

        // Card 48 not in present - cannot win, return unchanged state
        return s;
    }
}