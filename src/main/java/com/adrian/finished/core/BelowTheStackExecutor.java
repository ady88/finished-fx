
package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.*;

/**
 * AbilityExecutor for AbilitySpec.BELOW_THE_STACK.
 * Rules implemented (see game-abilities.json and AbilitySpec.BELOW_THE_STACK):
 * - First, player selects a card that provides this ability (cards 4, 7, 29, 42) to spend candy on
 * - Spend 1 candy and increment that card's abilitiesTriggered count
 * - Reset abilitiesTriggered to 0 for ALL present cards and return those candies to reserved stash
 * - Place all present cards under the draw pile in order and clear present area
 * - The END_TURN_END ability will be called next via the ability chain to handle past area cleanup
 */
public final class BelowTheStackExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.BELOW_THE_STACK) {
            throw new IllegalArgumentException("BelowTheStackExecutor can only execute BELOW_THE_STACK ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();
        List<Card> presentCards = s.present().cards();

        // Need at least 1 card in present (for the ability provider)
        if (presentCards.isEmpty()) {
            return s;
        }

        // Check if player has candy to spend
        if (s.activeStash().candy() <= 0) {
            return s; // Cannot execute without candy
        }

        // Find valid ability provider cards (cards that provide this ability)
        List<Integer> validProviders = new ArrayList<>();
        for (int i = 0; i < presentCards.size(); i++) {
            Card card = presentCards.get(i);
            if (context.ability().cards().contains(card.number())) {
                validProviders.add(i);
            }
        }

        // Must have at least one valid ability provider
        if (validProviders.isEmpty()) {
            return s; // Cannot execute without a valid ability provider
        }

        // Step 1: Player selects which ability provider card to spend candy on
        int providerIndex = context.provider().selectAbilityProviderCard(s,
                context.ability().cards());

        // Validate provider selection
        if (providerIndex < 0 || providerIndex >= presentCards.size()) {
            throw new IllegalStateException("Invalid ability provider index: " + providerIndex);
        }

        Card providerCard = presentCards.get(providerIndex);
        if (!context.ability().cards().contains(providerCard.number())) {
            throw new IllegalStateException("Selected card cannot provide this ability");
        }

        // Process candy cost
        Stash newActiveStash = new Stash(s.activeStash().candy() - 1, s.activeStash().coffee());

        // Determine whether provider has remaining capacity
        boolean providerHasCapacity = providerCard.abilitiesTriggered() < providerCard.maxAbilities();

        // If provider has reached limit AND draw stack is empty, we do nothing (return state unchanged)
        if (!providerHasCapacity && s.drawStack().cards().isEmpty()) {
            return s;
        }

        // Build the list of present cards to base candy return on:
        // - If provider has capacity, increment it before resetting so one more candy is returned from provider.
        // - If provider is at limit, do not increment, but still proceed with reset and moving cards under draw.
        List<Card> presentForReset;
        if (providerHasCapacity) {
            Card updatedProviderCard = new Card(providerCard.number(), providerCard.abilitiesTriggered() + 1,
                    providerCard.maxAbilities(), providerCard.fromDrawStack());
            List<Card> updatedPresentCards = new ArrayList<>(presentCards);
            updatedPresentCards.set(providerIndex, updatedProviderCard);
            presentForReset = updatedPresentCards;
        } else {
            presentForReset = presentCards;
        }

        // Count total candies to return (from ALL present cards, with provider possibly incremented)
        int candiesToReturn = 0;
        List<Card> resetPresentCards = new ArrayList<>();

        for (Card card : presentForReset) {
            candiesToReturn += card.abilitiesTriggered();
            // Reset all cards to 0 abilities (candies removed)
            Card resetCard = new Card(card.number(), 0, card.maxAbilities(), card.fromDrawStack());
            resetPresentCards.add(resetCard);
        }

        // Update reserved stash with returned candies
        Stash newReservedStash = new Stash(
                s.reservedStash().candy() + candiesToReturn,
                s.reservedStash().coffee()
        );

        // Place all present cards under the draw stack in order
        Deque<Card> newDrawStack = new ArrayDeque<>(s.drawStack().cards());
        for (Card card : resetPresentCards) {
            newDrawStack.addLast(card);
        }

        // Clear present area
        PresentArea newPresent = new PresentArea(List.of());

        return new GameState(
                newActiveStash,
                newReservedStash,
                new DrawStack(newDrawStack),
                newPresent,
                s.past(), // Past area unchanged - END_TURN_END will handle cleanup
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}