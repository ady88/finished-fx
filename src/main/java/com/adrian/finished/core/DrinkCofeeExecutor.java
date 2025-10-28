package com.adrian.finished.core;

import com.adrian.finished.model.GameState;
import com.adrian.finished.model.PresentArea;
import com.adrian.finished.model.Stash;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.Objects;

/**
 * AbilityExecutor for AbilitySpec.DRINK_COFEE.
 *
 * Rules implemented (see rule-files/game-abilities.json and AbilitySpec.DRINK_COFEE):
 * - If card 48 is in the Past area (moved this turn per game flow), automatically spend 1 coffee token
 *   from the active stash.
 * - If spending would go below 0, do not change the stash here (engine should proceed to GAME_END_LOSE).
 *
 * Note: The model doesn't support negative coffee values. We simply decrement if coffee > 0.
 */
public final class DrinkCofeeExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.DRINK_COFEE) {
            throw new IllegalArgumentException("DrinkCofeeExecutor can only execute DRINK_COFEE ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();

        boolean pastHas48 = s.past().cards().stream().anyMatch(c -> c.number() == 48);
        if (!pastHas48) {
            return s; // nothing to do
        }

        int coffee = s.activeStash().coffee();
        if (coffee <= 0) {
            // Cannot decrement due to model constraints; engine should transition to GAME_END_LOSE next.
            return s;
        }

        Stash newActive = new Stash(s.activeStash().candy(), coffee - 1);

        return new GameState(
                newActive,
                s.reservedStash(),
                s.drawStack(),
                new PresentArea(s.present().cards()),
                s.past(),
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}
