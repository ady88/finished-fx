package com.adrian.finished.ui.pipeline;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.GameState;
import com.adrian.finished.model.abilities.AbilitySpec;
import java.util.*;

/**
 * Determines which abilities can be activated based on the current game state
 * and cards present. Maps cards to their available abilities based on AbilitySpec definitions.
 */
public class AbilityActivationManager {

    // Map of card numbers to their abilities based on AbilitySpec
    private static final Map<Integer, List<AbilitySpec>> CARD_ABILITIES = createCardAbilityMap();

    /**
     * Get all abilities that can be activated for a specific card.
     */
    public List<AbilitySpec> getAvailableAbilities(Card card, GameState state, Set<AbilitySpec> usedAbilities) {
        List<AbilitySpec> cardAbilities = CARD_ABILITIES.getOrDefault(card.number(), List.of());
        List<AbilitySpec> available = new ArrayList<>();

        for (AbilitySpec ability : cardAbilities) {
            if (canActivateAbility(card, ability, state, usedAbilities)) {
                available.add(ability);
            }
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
        switch (ability) {
            case CARDS_FROM_PAST:
                // Need at least 2 cards in past
                return state.past().cards().size() >= 2;

            case EXCHANGE_PRESENT_CARD_ORDER:
                // Need at least 2 cards in present
                return state.present().cards().size() >= 2;

            case CARDS_INTO_PAST:
                // Need at least 2 cards in present
                return state.present().cards().size() >= 2;

            default:
                return true;
        }
    }

    /**
     * Get the primary ability for a card (the most commonly used one).
     */
    public AbilitySpec getPrimaryAbility(Card card) {
        List<AbilitySpec> abilities = CARD_ABILITIES.getOrDefault(card.number(), List.of());
        return abilities.isEmpty() ? null : abilities.get(0);
    }

    /**
     * Check if a card has any activatable abilities.
     */
    public boolean hasActivatableAbilities(Card card, GameState state, Set<AbilitySpec> usedAbilities) {
        return !getAvailableAbilities(card, state, usedAbilities).isEmpty();
    }

    /**
     * Create the mapping of card numbers to their abilities based on AbilitySpec.
     */
    private static Map<Integer, List<AbilitySpec>> createCardAbilityMap() {
        Map<Integer, List<AbilitySpec>> map = new HashMap<>();

        // Manual abilities with specific cards

        // DRAW_TWO - Card 2 only
        map.put(2, List.of(AbilitySpec.DRAW_TWO));

        // CARDS_INTO_PAST - Cards 5, 11, 17, 23, 25, 41
        List<AbilitySpec> cardsIntoPast = List.of(AbilitySpec.CARDS_INTO_PAST);
        map.put(5, cardsIntoPast);
        map.put(11, cardsIntoPast);
        map.put(17, cardsIntoPast);
        map.put(23, cardsIntoPast);
        map.put(25, cardsIntoPast);
        map.put(41, cardsIntoPast);

        // CARDS_FROM_PAST - Cards 8, 18, 30, 44
        List<AbilitySpec> cardsFromPast = List.of(AbilitySpec.CARDS_FROM_PAST);
        map.put(8, cardsFromPast);
        map.put(18, cardsFromPast);
        map.put(30, cardsFromPast);
        map.put(44, cardsFromPast);

        // DRAW_ONE - Cards 9, 14, 20, 27, 31, 34, 46
        List<AbilitySpec> drawOne = List.of(AbilitySpec.DRAW_ONE);
        map.put(9, drawOne);
        map.put(14, drawOne);
        map.put(20, drawOne);
        map.put(27, drawOne);
        map.put(31, drawOne);
        map.put(34, drawOne);
        map.put(46, drawOne);

        // CARD_INTO_FUTURE - Cards 12, 19, 32, 40
        List<AbilitySpec> cardIntoFuture = List.of(AbilitySpec.CARD_INTO_FUTURE);
        map.put(12, cardIntoFuture);
        map.put(19, cardIntoFuture);
        map.put(32, cardIntoFuture);
        map.put(40, cardIntoFuture);

        // EXCHANGE_CARD - Cards 13, 22, 33, 39, 43
        List<AbilitySpec> exchangeCard = List.of(AbilitySpec.EXCHANGE_CARD);
        map.put(13, exchangeCard);
        map.put(22, exchangeCard);
        map.put(33, exchangeCard);
        map.put(39, exchangeCard);
        map.put(43, exchangeCard);

        // DRAW_ONE_3X - Card 47 only
        map.put(47, List.of(AbilitySpec.DRAW_ONE_3X));

        // Universal abilities (can be used with any card, but we'll add them to specific cards for UI purposes)
        // EXCHANGE_PRESENT_CARD_ORDER - Add to cards that don't have other abilities
        List<AbilitySpec> exchangeOrder = List.of(AbilitySpec.EXCHANGE_PRESENT_CARD_ORDER);

        // Add exchange order to cards without other abilities (just a few examples)
        if (!map.containsKey(1)) map.put(1, exchangeOrder);
        if (!map.containsKey(3)) map.put(3, exchangeOrder);
        if (!map.containsKey(4)) map.put(4, exchangeOrder);
        if (!map.containsKey(6)) map.put(6, exchangeOrder);
        if (!map.containsKey(7)) map.put(7, exchangeOrder);
        if (!map.containsKey(10)) map.put(10, exchangeOrder);
        if (!map.containsKey(15)) map.put(15, exchangeOrder);
        if (!map.containsKey(16)) map.put(16, exchangeOrder);
        if (!map.containsKey(21)) map.put(21, exchangeOrder);
        if (!map.containsKey(24)) map.put(24, exchangeOrder);
        if (!map.containsKey(26)) map.put(26, exchangeOrder);
        if (!map.containsKey(28)) map.put(28, exchangeOrder);
        if (!map.containsKey(29)) map.put(29, exchangeOrder);
        if (!map.containsKey(35)) map.put(35, exchangeOrder);
        if (!map.containsKey(36)) map.put(36, exchangeOrder);
        if (!map.containsKey(37)) map.put(37, exchangeOrder);
        if (!map.containsKey(38)) map.put(38, exchangeOrder);
        if (!map.containsKey(42)) map.put(42, exchangeOrder);
        if (!map.containsKey(45)) map.put(45, exchangeOrder);
        if (!map.containsKey(48)) map.put(48, exchangeOrder);

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
