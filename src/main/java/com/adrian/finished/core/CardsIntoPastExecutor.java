
package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.*;

/**
 * AbilityExecutor for AbilitySpec.CARDS_INTO_PAST.
 *
 * Rules implemented (see game-abilities.json and AbilitySpec.CARDS_INTO_PAST):
 * - First, player selects a card that provides this ability (cards 5, 11, 17, 23, 25, 41) to spend candy on
 * - Spend 1 candy and increment that card's abilitiesTriggered count
 * - Then, player selects 2 cards from the present area to move into the past pile
 * - Draw 2 new cards from the draw stack into the present area
 * - If there are no cards in the draw pile, cards are retrieved from the past area instead
 * - Requires two user selections via DecisionProvider
 */
public final class CardsIntoPastExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.CARDS_INTO_PAST) {
            throw new IllegalArgumentException("CardsIntoPastExecutor can only execute CARDS_INTO_PAST ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();
        List<Card> presentCards = s.present().cards();

        // Must have at least 2 cards in present to select
        if (presentCards.size() < 2) {
            return s; // Cannot execute if insufficient cards
        }

        // Check if player has candy to spend
        if (s.activeStash().candy() <= 0) {
            return s; // Cannot execute without candy
        }

        // Find valid ability provider cards (cards that provide this ability and haven't reached limit)
        List<Integer> validProviders = new ArrayList<>();
        for (int i = 0; i < presentCards.size(); i++) {
            Card card = presentCards.get(i);
            if (context.ability().cards().contains(card.number()) &&
                    card.abilitiesTriggered() < card.maxAbilities()) {
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
        if (!context.ability().cards().contains(providerCard.number()) ||
                providerCard.abilitiesTriggered() >= providerCard.maxAbilities()) {
            throw new IllegalStateException("Selected card cannot provide this ability or has reached usage limit");
        }

        // Process candy cost and ability tracking for the provider card
        Stash newActiveStash = new Stash(s.activeStash().candy() - 1, s.activeStash().coffee());
        Card updatedProviderCard = new Card(providerCard.number(), providerCard.abilitiesTriggered() + 1,
                providerCard.maxAbilities(), providerCard.fromDrawStack());

        // Update present cards with the ability tracking
        List<Card> updatedPresentCards = new ArrayList<>(presentCards);
        updatedPresentCards.set(providerIndex, updatedProviderCard);

        // Step 2: Player selects 2 cards to move to past
        List<Integer> selectedIndices = context.provider().selectPresentCardIndices(s, 2);

        // Validate selection
        if (selectedIndices.size() != 2) {
            throw new IllegalStateException("Expected exactly 2 card selections, got: " + selectedIndices.size());
        }

        // Ensure indices are valid and unique
        for (int index : selectedIndices) {
            if (index < 0 || index >= updatedPresentCards.size()) {
                throw new IllegalStateException("Invalid card index: " + index);
            }
        }
        if (selectedIndices.get(0).equals(selectedIndices.get(1))) {
            throw new IllegalStateException("Selected indices must be different");
        }

        // Collect selected cards in selection order before removing them
        List<Card> selectedCards = new ArrayList<>(2);
        selectedCards.add(updatedPresentCards.get(selectedIndices.get(0)));
        selectedCards.add(updatedPresentCards.get(selectedIndices.get(1)));

        // Remove selected cards from present (in reverse index order to maintain indices)
        List<Integer> sortedIndices = new ArrayList<>(selectedIndices);
        sortedIndices.sort((a, b) -> Integer.compare(b, a)); // Sort descending

        List<Card> newPresentCards = new ArrayList<>(updatedPresentCards);
        for (int index : sortedIndices) {
            newPresentCards.remove(index);
        }

        // Add selected cards to past in the order they were originally selected
        Deque<Card> newPast = new ArrayDeque<>(s.past().cards());
        for (Card card : selectedCards) {
            newPast.addLast(card);
        }

        // Draw 2 replacement cards: from draw stack if available, otherwise from past
        Deque<Card> drawSource = new ArrayDeque<>(s.drawStack().cards());
        List<Card> replacements = new ArrayList<>(2);

        for (int i = 0; i < 2; i++) {
            Card replacement = null;
            if (!drawSource.isEmpty()) {
                replacement = drawSource.removeFirst();
            } else if (!newPast.isEmpty()) {
                Card fromPast = newPast.removeFirst();
                // Mark as not from draw stack when retrieved from past
                replacement = new Card(fromPast.number(), fromPast.abilitiesTriggered(), fromPast.maxAbilities(), false);
            }
            if (replacement != null) {
                replacements.add(replacement);
            }
        }

        // Add replacement cards to present
        newPresentCards.addAll(replacements);

        return new GameState(
                newActiveStash,
                s.reservedStash(),
                new DrawStack(drawSource),
                new PresentArea(List.copyOf(newPresentCards)),
                new PastArea(newPast),
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}