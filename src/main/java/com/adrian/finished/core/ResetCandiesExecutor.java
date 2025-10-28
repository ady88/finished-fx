package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AbilityExecutor for AbilitySpec.RESET_CANDIES.
 * Rules implemented (see game-abilities.json and AbilitySpec.RESET_CANDIES):
 * - First, player selects card 37 (the only card that provides this ability) to spend candy on
 * - Spend 1 candy and increment that card's abilitiesTriggered count
 * - Remove all candy currently placed on any cards in the present and future areas,
 *   EXCEPT the one placed on card 37 itself
 * - Place these removed candies back in the reserved stash
 * - This allows players to reactivate abilities that have reached their usage limits
 */
public final class ResetCandiesExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.RESET_CANDIES) {
            throw new IllegalArgumentException("ResetCandiesExecutor can only execute RESET_CANDIES ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();
        List<Card> presentCards = s.present().cards();

        // Need at least 1 card in present (for card 37)
        if (presentCards.isEmpty()) {
            return s;
        }

        // Check if player has candy to spend
        if (s.activeStash().candy() <= 0) {
            return s; // Cannot execute without candy
        }

        // Find card 37 in present area that can provide this ability
        int card37Index = -1;
        for (int i = 0; i < presentCards.size(); i++) {
            Card card = presentCards.get(i);
            if (card.number() == 37 && card.abilitiesTriggered() < card.maxAbilities()) {
                card37Index = i;
                break;
            }
        }

        // Must have card 37 available to use
        if (card37Index == -1) {
            return s; // Cannot execute without a usable card 37
        }

        // Step 1: Player selects card 37 as ability provider
        int providerIndex = context.provider().selectAbilityProviderCard(s,
                context.ability().cards());

        // Validate provider selection
        if (providerIndex < 0 || providerIndex >= presentCards.size()) {
            throw new IllegalStateException("Invalid ability provider index: " + providerIndex);
        }

        Card providerCard = presentCards.get(providerIndex);
        if (providerCard.number() != 37 ||
                providerCard.abilitiesTriggered() >= providerCard.maxAbilities()) {
            throw new IllegalStateException("Selected card must be card 37 and not have reached usage limit");
        }

        // Process candy cost and ability tracking for card 37
        Stash newActiveStash = new Stash(s.activeStash().candy() - 1, s.activeStash().coffee());
        Card updatedCard37 = new Card(37, providerCard.abilitiesTriggered() + 1,
                providerCard.maxAbilities(), providerCard.fromDrawStack());

        // Count candies to return from present area (all cards except card 37)
        int candiesToReturn = 0;
        List<Card> newPresentCards = new ArrayList<>(presentCards);

        for (int i = 0; i < newPresentCards.size(); i++) {
            Card card = newPresentCards.get(i);
            if (i == providerIndex) {
                // This is card 37 - update with ability tracking but preserve its candies
                newPresentCards.set(i, updatedCard37);
            } else {
                // Reset abilities for all other cards and count their candies
                candiesToReturn += card.abilitiesTriggered();
                Card resetCard = new Card(card.number(), 0, card.maxAbilities(), card.fromDrawStack());
                newPresentCards.set(i, resetCard);
            }
        }

        // Count and reset candies from future areas
        List<FutureArea> newFutureAreas = new ArrayList<>();
        for (FutureArea futureArea : s.futureAreas()) {
            List<Card> newFutureCards = new ArrayList<>();
            for (Card card : futureArea.cards()) {
                candiesToReturn += card.abilitiesTriggered();
                Card resetCard = new Card(card.number(), 0, card.maxAbilities(), card.fromDrawStack());
                newFutureCards.add(resetCard);
            }
            newFutureAreas.add(new FutureArea(List.copyOf(newFutureCards)));
        }

        // Update reserved stash with returned candies
        Stash newReservedStash = new Stash(
                s.reservedStash().candy() + candiesToReturn,
                s.reservedStash().coffee()
        );

        return new GameState(
                newActiveStash,
                newReservedStash,
                s.drawStack(),
                new PresentArea(List.copyOf(newPresentCards)),
                s.past(),
                List.copyOf(newFutureAreas),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}