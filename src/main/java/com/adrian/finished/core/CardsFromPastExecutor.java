package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.*;

/**
 * AbilityExecutor for AbilitySpec.CARDS_FROM_PAST.
 *
 * Rules implemented (see game-abilities.json and AbilitySpec.CARDS_FROM_PAST):
 * - First, player selects a card that provides this ability (cards 8, 18, 30, 44) to spend candy on
 * - Spend 1 candy and increment that card's abilitiesTriggered count
 * - Then, retrieve up to the last 2 cards from the Past area (i.e., the most recently added two) into Present
 * - Retrieved cards are appended to the Present area and marked fromDrawStack = false
 * - If there are fewer than 2 cards in Past, move as many as available (0, 1, or 2)
 * - If no valid provider or no candy, return state unchanged
 */
public final class CardsFromPastExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.CARDS_FROM_PAST) {
            throw new IllegalArgumentException("CardsFromPastExecutor can only execute CARDS_FROM_PAST ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();
        List<Card> presentCards = s.present().cards();

        // Must have candy to spend
        if (s.activeStash().candy() <= 0) {
            return s; // Cannot execute without candy
        }

        // Find at least one valid ability provider in present
        boolean hasProvider = false;
        for (Card card : presentCards) {
            if (context.ability().cards().contains(card.number()) && card.abilitiesTriggered() < card.maxAbilities()) {
                hasProvider = true;
                break;
            }
        }
        if (!hasProvider) {
            return s; // Cannot execute without a valid ability provider
        }

        // Step 1: Player selects which ability provider card to spend candy on
        int providerIndex = context.provider().selectAbilityProviderCard(s, context.ability().cards());
        if (providerIndex < 0 || providerIndex >= presentCards.size()) {
            throw new IllegalStateException("Invalid ability provider index: " + providerIndex);
        }
        Card providerCard = presentCards.get(providerIndex);
        if (!context.ability().cards().contains(providerCard.number()) ||
                providerCard.abilitiesTriggered() >= providerCard.maxAbilities()) {
            throw new IllegalStateException("Selected card cannot provide this ability or has reached usage limit");
        }

        // Spend candy and increment provider's abilitiesTriggered
        Stash newActiveStash = new Stash(s.activeStash().candy() - 1, s.activeStash().coffee());
        Card updatedProviderCard = new Card(providerCard.number(), providerCard.abilitiesTriggered() + 1,
                providerCard.maxAbilities(), providerCard.fromDrawStack());

        List<Card> newPresent = new ArrayList<>(presentCards);
        newPresent.set(providerIndex, updatedProviderCard);

        // Step 2: Move up to the last two cards from Past into Present
        Deque<Card> newPast = new ArrayDeque<>(s.past().cards());
        List<Card> movedFromPast = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            if (newPast.isEmpty()) break;
            Card c = newPast.removeLast(); // take newest first
            movedFromPast.add(c);
        }
        // We want to append them in chronological order (older before newer)
        Collections.reverse(movedFromPast); // now older among the taken is first

        for (Card c : movedFromPast) {
            // Mark as not from draw stack when retrieved from past
            newPresent.add(new Card(c.number(), c.abilitiesTriggered(), c.maxAbilities(), false));
        }

        return new GameState(
                newActiveStash,
                s.reservedStash(),
                s.drawStack(),
                new PresentArea(List.copyOf(newPresent)),
                new PastArea(newPast),
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}
