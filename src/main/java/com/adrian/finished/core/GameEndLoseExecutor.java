package com.adrian.finished.core;

import com.adrian.finished.model.GameState;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.Objects;

/**
 * AbilityExecutor for AbilitySpec.GAME_END_LOSE.
 *
 * According to the AbilitySpec description: "If card 48 is moved to the past and there are no more coffee tokens left player loses."
 * This executor checks if card 48 is in the past area and active stash has 0 coffee tokens.
 * If both conditions are met, sets the gameEnd flag to true, indicating the player has lost.
 */
public final class GameEndLoseExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.GAME_END_LOSE) {
            throw new IllegalArgumentException("GameEndLoseExecutor can only execute GAME_END_LOSE ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();

        // Check losing conditions: card 48 in past AND no coffee tokens left
        boolean card48InPast = s.past().cards().stream().anyMatch(card -> card.number() == 48);
        boolean noCoffeeLeft = s.activeStash().coffee() <= 0;

        if (card48InPast && noCoffeeLeft) {
            // Player loses - set gameEnd flag to true
            return new GameState(
                    s.activeStash(),
                    s.reservedStash(),
                    s.drawStack(),
                    s.present(),
                    s.past(),
                    s.futureAreas(),
                    s.finishedPile(),
                    s.activeAllCardsInFutureAreas(),
                    true
            );
        }

        // Losing conditions not met - no change to gameEnd flag
        return s;
    }
}