package com.adrian.finished.ui.pipeline;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.GameState;
import com.adrian.finished.model.abilities.AbilityPhase;
import com.adrian.finished.model.abilities.AbilitySpec;
import java.util.*;

/**
 * Determines which abilities can be activated based on the current game state
 * and cards present. Maps cards to their available abilities based on AbilitySpec definitions.
 */
public class AbilityActivationManager {

    // Map of card numbers to their abilities based on AbilitySpec
    private static final Map<Integer, AbilitySpec> CARD_ABILITIES = createCardAbilityMap();

    /**
     * Get all abilities that can be activated for a specific card.
     */
    public List<AbilitySpec> getAvailableAbilities(Card card, GameState state, Set<AbilitySpec> usedAbilities) {
        AbilitySpec cardAbility = CARD_ABILITIES.getOrDefault(card.number(), null);
        List<AbilitySpec> available = new ArrayList<>();

        if (canActivateAbility(card, cardAbility, state, usedAbilities)) {
            available.add(cardAbility);
        }

        return available;
    }

    /**
     * Check if a specific ability can be activated on a card.
     */
    public boolean canActivateAbility(Card card, AbilitySpec ability, GameState state, Set<AbilitySpec> usedAbilities) {
        // Check if ability has already been used this turn
        if (usedAbilities.contains(ability)) {
            return false;
        }

        // Check if card still has ability uses remaining
        if (ability.requiresCandy() && !card.canTriggerAbility()) {
            return false;
        }

        // Check if player has enough candy in active stash
        if (ability.requiresCandy() && state.activeStash().candy() <= 0) {
            return false;
        }

        // Only manual abilities can be activated by user
        if (ability.phase() != com.adrian.finished.model.abilities.AbilityPhase.USER_INPUT_REQUIRED) {
            return false;
        }

        // Specific ability checks
        return switch (ability) {
            case EXCHANGE_PRESENT_CARD_ORDER ->
                // Need at least 2 cards in present
                    state.present().cards().size() >= 2;
            case CARDS_INTO_PAST ->
                // Need at least 2 cards in present
                    state.present().cards().size() >= 2;
            default -> true;
        };
    }

    /**
     * Check if a card has any activatable abilities.
     */
//    public boolean hasActivatableAbilities(Card card, GameState state, Set<AbilitySpec> usedAbilities) {
//        return !getAvailableAbilities(card, state, usedAbilities).isEmpty();
//    }

    /**
     * Create the mapping of card numbers to their abilities based on AbilitySpec.
     * This method automatically builds the mapping from the AbilitySpec enum data,
     * eliminating hardcoded card numbers.
     */
    private static Map<Integer, AbilitySpec> createCardAbilityMap() {
        Map<Integer, AbilitySpec> map = new HashMap<>();

        // Iterate through all abilities and build the card->ability mapping
        for (AbilitySpec ability : AbilitySpec.values()) {
            // Skip EXCHANGE_PRESENT_CARD_ORDER as it's a free action available to any card
            if (ability == AbilitySpec.EXCHANGE_PRESENT_CARD_ORDER) {
                continue;
            }

            // Skip automatic abilities that aren't card-specific
            if (ability.phase() != AbilityPhase.USER_INPUT_REQUIRED) {
                continue;
            }

            // Add each card number to the map with its corresponding ability
            for (Integer cardNumber : ability.cards()) {
                if (map.containsKey(cardNumber)) {
                    // This shouldn't happen based on your statement that each card has at most one ability
                    throw new IllegalStateException("Card " + cardNumber + " already has an ability: " + map.get(cardNumber) +
                            ". Attempting to add: " + ability);
                }
                map.put(cardNumber, ability);
            }
        }

        return map;
    }


    /**
     * Get a human-readable description of an ability.
     */
    public String getAbilityDescription(AbilitySpec ability) {
        return switch (ability) {
            case DRAW_TWO -> "Draw 2 cards (1 candy)";
            case CARDS_INTO_PAST -> "Move 2 cards to past (1 candy)";
            case DRAW_ONE -> "Draw 1 card (1 candy)";
            case DRAW_ONE_3X -> "Draw 1-3 cards (1 candy each)";
            case EXCHANGE_CARD -> "Exchange 1 card (1 candy)";
            case CARDS_FROM_PAST -> "Take 2 cards from past (1 candy)";
            case CARD_INTO_FUTURE -> "Move 1 card to future (1 candy)";
            case EXCHANGE_PRESENT_CARD_ORDER -> "Swap 2 cards (free)";
            case RESET_CANDIES -> "Reset candy count (special)";
            default -> ability.name();
        };
    }

    /**
     * Check if a card has the "take candy" symbol.
     */
    public boolean hasTakeCandySymbol(Card card) {
        // Cards with takeCandy symbol: 3, 6, 10, 15, 21, 28, 36, 45
        int number = card.number();
        return number == 3 || number == 6 || number == 10 || number == 15 ||
               number == 21 || number == 28 || number == 36 || number == 45;
    }
}
