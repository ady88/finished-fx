package com.adrian.finished.core;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.GameState;
import com.adrian.finished.model.PresentArea;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AbilityExecutor for AbilitySpec.EXCHANGE_PRESENT_CARD_ORDER.
 * Rules implemented (see game-abilities.json and AbilitySpec.EXCHANGE_PRESENT_CARD_ORDER):
 * - Player selects 2 cards from the present area
 * - Swap the positions of those 2 cards
 * - No candy cost required
 * - Cards themselves are unchanged, only their order in the present area
 */
public final class ExchangePresentCardOrderExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.EXCHANGE_PRESENT_CARD_ORDER) {
            throw new IllegalArgumentException("ExchangePresentCardOrderExecutor can only execute EXCHANGE_PRESENT_CARD_ORDER ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();
        List<Card> presentCards = s.present().cards();

        // Need at least 2 cards in present to swap
        if (presentCards.size() < 2) {
            return s; // Cannot swap with fewer than 2 cards
        }

        // Player selects exactly 2 cards to swap
        List<Integer> selectedIndices = context.provider().selectPresentCardIndices(s, 2);

        // Validate selection
        if (selectedIndices.size() != 2) {
            throw new IllegalStateException("Must select exactly 2 cards to swap, got: " + selectedIndices.size());
        }

        int index1 = selectedIndices.get(0);
        int index2 = selectedIndices.get(1);

        // Validate indices are within bounds
        if (index1 < 0 || index1 >= presentCards.size()) {
            throw new IllegalStateException("Invalid card index: " + index1);
        }
        if (index2 < 0 || index2 >= presentCards.size()) {
            throw new IllegalStateException("Invalid card index: " + index2);
        }

        // If both indices are the same, no change needed
        if (index1 == index2) {
            return s; // No swap needed
        }

        // Create new present area with swapped cards
        List<Card> newPresentCards = new ArrayList<>(presentCards);
        Card card1 = newPresentCards.get(index1);
        Card card2 = newPresentCards.get(index2);

        // Swap the cards at the two positions
        newPresentCards.set(index1, card2);
        newPresentCards.set(index2, card1);

        // Return new game state with updated present area
        return new GameState(
                s.activeStash(),      // Unchanged - no candy cost
                s.reservedStash(),    // Unchanged
                s.drawStack(),        // Unchanged
                new PresentArea(List.copyOf(newPresentCards)), // Updated with swapped cards
                s.past(),             // Unchanged
                s.futureAreas(),      // Unchanged
                s.finishedPile(),     // Unchanged
                s.activeAllCardsInFutureAreas() // Unchanged
        );
    }
}