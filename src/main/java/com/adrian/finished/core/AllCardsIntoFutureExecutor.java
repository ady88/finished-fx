package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.*;

/**
 * AbilityExecutor for AbilitySpec.ALL_CARDS_INTO_FUTURE.
 *
 * Implements the rules from game-abilities.json:
 * - Player selects a card that provides this ability and can still be used this turn; spend 1 candy
 *   and increment that card's abilitiesTriggered count.
 * - If this is the first activation (counter == 0): move ALL present cards into a new first future area,
 *   mark them as fromDrawStack=false, clear present, increment activeAllCardsInFutureAreas by 1,
 *   and immediately draw up to 3 cards to the present area from the draw stack (or, if empty, from past),
 *   marking cards drawn from past with fromDrawStack=false.
 * - If this ability was already active (counter > 0): push the existing first future area deeper (becomes second),
 *   place ALL current present cards into the now-first future area (mark fromDrawStack=false), increment counter,
 *   and immediately draw up to 3 cards to the present area from the draw stack (or, if empty, from past),
 *   marking cards drawn from past with fromDrawStack=false.
 */
public final class AllCardsIntoFutureExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.ALL_CARDS_INTO_FUTURE) {
            throw new IllegalArgumentException("AllCardsIntoFutureExecutor can only execute ALL_CARDS_INTO_FUTURE ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();
        List<Card> presentCards = s.present().cards();

        // Require candy
        if (s.activeStash().candy() <= 0) {
            return s;
        }

        // Find valid provider indices (cards that can still provide this ability)
        List<Integer> validProviderIndices = new ArrayList<>();
        for (int i = 0; i < presentCards.size(); i++) {
            Card c = presentCards.get(i);
            if (context.ability().cards().contains(c.number()) && c.abilitiesTriggered() < c.maxAbilities()) {
                validProviderIndices.add(i);
            }
        }
        if (validProviderIndices.isEmpty()) {
            return s; // no valid card to spend candy on
        }

        int providerIndex = context.provider().selectAbilityProviderCard(s, context.ability().cards());
        if (providerIndex < 0 || providerIndex >= presentCards.size()) {
            throw new IllegalStateException("Invalid ability provider index: " + providerIndex);
        }
        Card providerCard = presentCards.get(providerIndex);
        if (!context.ability().cards().contains(providerCard.number()) ||
                providerCard.abilitiesTriggered() >= providerCard.maxAbilities()) {
            throw new IllegalStateException("Selected card cannot provide this ability or has reached usage limit");
        }

        // Spend 1 candy and increment abilitiesTriggered on the selected provider card
        Stash newActiveStash = new Stash(s.activeStash().candy() - 1, s.activeStash().coffee());
        Card updatedProviderCard = new Card(providerCard.number(), providerCard.abilitiesTriggered() + 1,
                providerCard.maxAbilities(), providerCard.fromDrawStack());
        List<Card> updatedPresent = new ArrayList<>(presentCards);
        updatedPresent.set(providerIndex, updatedProviderCard);

        // Prepare future areas and present/draw/past updates
        int counter = s.activeAllCardsInFutureAreas();
        List<FutureArea> newFutures = new ArrayList<>();
        PresentArea newPresentArea;
        DrawStack newDrawStack = s.drawStack();
        PastArea newPastArea = s.past();

        // Convert all present cards to fromDrawStack=false when moving into future
        List<Card> toFuture = new ArrayList<>(updatedPresent.size());
        for (Card c : updatedPresent) {
            toFuture.add(new Card(c.number(), c.abilitiesTriggered(), c.maxAbilities(), false));
        }

        if (counter == 0) {
            // First activation: move present to new future, clear present, then draw up to 3 cards for present
            newFutures.add(new FutureArea(List.copyOf(toFuture)));
            newFutures.addAll(s.futureAreas());

            // Use DrawHelpers to draw up to 3 cards for the present area
            DrawHelpers.Result drawResult = DrawHelpers.drawUpTo(3, s.drawStack(), s.past());
            newPresentArea = new PresentArea(List.copyOf(drawResult.drawn));
            newDrawStack = drawResult.newDraw;
            newPastArea = drawResult.newPast;
        } else {
            // Already have future areas active: shift first deeper and place present into first future area
            // First, the new first future area is the current present (toFuture)
            newFutures.add(new FutureArea(List.copyOf(toFuture)));
            // Then all existing future areas follow as-is (first becomes second, etc.)
            newFutures.addAll(s.futureAreas());

            // Draw up to 3 cards for present area using DrawHelpers
            DrawHelpers.Result drawResult = DrawHelpers.drawUpTo(3, s.drawStack(), s.past());
            newPresentArea = new PresentArea(List.copyOf(drawResult.drawn));
            newDrawStack = drawResult.newDraw;
            newPastArea = drawResult.newPast;
        }


        int newCounter = counter + 1;

        return new GameState(
                newActiveStash,
                s.reservedStash(),
                newDrawStack,
                newPresentArea,
                newPastArea,
                List.copyOf(newFutures),
                s.finishedPile(),
                newCounter
        );
    }
}