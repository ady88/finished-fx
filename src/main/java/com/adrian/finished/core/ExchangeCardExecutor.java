package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.*;

/**
 * AbilityExecutor for AbilitySpec.EXCHANGE_CARD.
 * Rules implemented (see game-abilities.json and AbilitySpec.EXCHANGE_CARD):
 * - First, player selects a card that provides this ability (cards 13, 22, 33, 39, 43) to spend candy on
 * - Spend 1 candy and increment that card's abilitiesTriggered count
 * - Draw one card from the draw stack and place it into the present area
 * - Then player selects any one card from the present area (including the newly drawn card)
 *   to place face down on top of the draw stack
 * - Requires two user selections via DecisionProvider
 */
public final class ExchangeCardExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.EXCHANGE_CARD) {
            throw new IllegalArgumentException("ExchangeCardExecutor can only execute EXCHANGE_CARD ability");
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

        // Check if draw stack has at least 1 card to exchange
        if (s.drawStack().cards().isEmpty()) {
            return s; // Cannot execute without cards to draw from
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

        // Draw one card from the draw stack and add it to present
        Deque<Card> drawStack = new ArrayDeque<>(s.drawStack().cards());
        Card drawnCard = drawStack.removeFirst();
        updatedPresentCards.add(drawnCard); // Add to the end of present area

        // Step 2: Player selects which card from present to put back on top of draw stack
        List<Integer> selected = context.provider().selectPresentCardIndices(s, 1);
        if (selected.size() != 1) {
            throw new IllegalStateException("Expected exactly 1 card selection for placing on draw stack, got: " + selected.size());
        }
        int exchangeIndex = selected.getFirst();

        // Validate exchange selection (use updated present cards which includes the drawn card)
        if (exchangeIndex < 0 || exchangeIndex >= updatedPresentCards.size()) {
            throw new IllegalStateException("Invalid card index for exchange: " + exchangeIndex);
        }

        // Remove the selected card from present and place it on top of draw stack
        Card cardToExchange = updatedPresentCards.get(exchangeIndex);
        updatedPresentCards.remove(exchangeIndex);
        drawStack.addFirst(cardToExchange); // Place on top of draw stack

        return new GameState(
                newActiveStash,
                s.reservedStash(),
                new DrawStack(drawStack),
                new PresentArea(List.copyOf(updatedPresentCards)),
                s.past(),
                s.futureAreas(),
                s.finishedPile(),
                s.activeAllCardsInFutureAreas()
        );
    }
}