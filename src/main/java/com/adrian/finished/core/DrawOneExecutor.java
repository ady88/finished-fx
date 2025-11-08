package com.adrian.finished.core;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.GameState;
import com.adrian.finished.model.PresentArea;
import com.adrian.finished.model.Stash;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AbilityExecutor for AbilitySpec.DRAW_ONE.
 *
 * Rules implemented (see game-abilities.json and AbilitySpec.DRAW_ONE):
 * - Player selects a present card that provides this ability and hasn't reached its limit
 * - Spend 1 candy and increment that card's abilitiesTriggered
 * - Draw one additional card: from draw stack if available, otherwise from the end of Past
 * - Retrieved card is appended to Present and marked fromDrawStack=false when coming from Past
 * - If both sources are empty, candy is still spent and no card is drawn ("try to draw one")
 */
public final class DrawOneExecutor implements AbilityExecutor {
    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.DRAW_ONE) {
            throw new IllegalArgumentException("DrawOneExecutor can only execute DRAW_ONE ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();
        List<Card> presentCards = s.present().cards();

        if (presentCards.isEmpty()) {
            return s;
        }
        if (s.activeStash().candy() <= 0) {
            return s;
        }

        // Ensure there is at least one eligible provider
        boolean hasProvider = false;
        for (Card card : presentCards) {
            if (context.ability().cards().contains(card.number()) && card.abilitiesTriggered() < card.maxAbilities()) {
                hasProvider = true; break;
            }
        }
        if (!hasProvider) {
            return s;
        }

        int providerIndex = context.provider().selectAbilityProviderCard(s, context.ability().cards());
        if (providerIndex < 0 || providerIndex >= presentCards.size()) {
            throw new IllegalStateException("Invalid ability provider index: " + providerIndex);
        }
        Card providerCard = presentCards.get(providerIndex);
        if (!context.ability().cards().contains(providerCard.number()) || providerCard.abilitiesTriggered() >= providerCard.maxAbilities()) {
            throw new IllegalStateException("Selected card cannot provide this ability or has reached usage limit");
        }

        // Spend candy and increment provider
        Stash newActiveStash = new Stash(s.activeStash().candy() - 1, s.activeStash().coffee());
        Card updatedProviderCard = new Card(providerCard.number(), providerCard.abilitiesTriggered() + 1,
                providerCard.maxAbilities(), providerCard.fromDrawStack());

        List<Card> newPresent = new ArrayList<>(presentCards);
        newPresent.set(providerIndex, updatedProviderCard);

        // Draw up to one card
        DrawHelpers.Result res = DrawHelpers.drawUpTo(1, s.drawStack(), s.past());
        newPresent.addAll(res.drawn);

        return new GameState(
                newActiveStash,
                s.reservedStash(),
                res.newDraw,
                new PresentArea(List.copyOf(newPresent)),
                res.newPast,
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}
