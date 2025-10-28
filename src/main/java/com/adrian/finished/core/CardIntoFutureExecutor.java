package com.adrian.finished.core;

import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.AbilityContext;
import com.adrian.finished.model.abilities.AbilityExecutor;
import com.adrian.finished.model.abilities.AbilitySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AbilityExecutor for AbilitySpec.CARD_INTO_FUTURE.
 * Rules implemented (see game-abilities.json and AbilitySpec.CARD_INTO_FUTURE):
 * - First, player selects a card that provides this ability (cards 12, 19, 32, 40) to spend candy on
 * - Spend 1 candy and increment that card's abilitiesTriggered count
 * - Then, player selects any card from present area to move into a new future area
 * - Increase activeAllCardsInFutureAreas by 1 so that at the next turn start,
 *   BeginTurnExecutor will resolve that future area back into present instead of drawing
 * - The moved card must not count as "from draw stack" on the next turn, so we set
 *   its fromDrawStack flag to false when placing into future
 * - Requires two user selections via DecisionProvider
 */
public final class CardIntoFutureExecutor implements AbilityExecutor {

    @Override
    public GameState apply(AbilityContext context) {
        if (context.ability() != AbilitySpec.CARD_INTO_FUTURE) {
            throw new IllegalArgumentException("CardIntoFutureExecutor can only execute CARD_INTO_FUTURE ability");
        }
        Objects.requireNonNull(context.state(), "state");

        GameState s = context.state();
        List<Card> presentCards = s.present().cards();

        // Need at least 1 card to move
        if (presentCards.isEmpty()) {
            return s;
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

        // Step 2: Player selects which card to move to future
        List<Integer> selected = context.provider().selectPresentCardIndices(s, 1);
        if (selected.size() != 1) {
            throw new IllegalStateException("Expected exactly 1 card selection for moving to future, got: " + selected.size());
        }
        int moveIndex = selected.getFirst();
        Card futureCard = getCard(moveIndex, updatedPresentCards);

        // Build new present area without the chosen card
        List<Card> newPresent = new ArrayList<>(updatedPresentCards);
        newPresent.remove(moveIndex);

        // Prepend a new future area containing the chosen card
        List<FutureArea> newFutures = new ArrayList<>();
        newFutures.add(new FutureArea(List.of(futureCard)));
        newFutures.addAll(s.futureAreas());

        int newCounter = s.activeAllCardsInFutureAreas() + 1;

        return new GameState(
                newActiveStash,
                s.reservedStash(),
                s.drawStack(),
                new PresentArea(List.copyOf(newPresent)),
                s.past(),
                List.copyOf(newFutures),
                s.finishedPile(),
                newCounter
        );
    }

    private static Card getCard(int moveIndex, List<Card> updatedPresentCards) {
        if (moveIndex < 0 || moveIndex >= updatedPresentCards.size()) {
            throw new IllegalStateException("Invalid card index for moving to future: " + moveIndex);
        }

        // Card chosen to move (use updated present cards which includes ability tracking)
        Card chosenToMove = updatedPresentCards.get(moveIndex);

        // Prepare card for future (fromDrawStack=false for future resolution)
        return new Card(chosenToMove.number(), chosenToMove.abilitiesTriggered(),
                chosenToMove.maxAbilities(), false);
    }
}