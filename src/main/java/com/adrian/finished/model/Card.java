package com.adrian.finished.model;

/**
 * Immutable representation of a playing card in Finished! game.
 * Cards are numbered from 1 to 48.
 *
 * Additional fields support per-card ability usage caps:
 * - abilitiesTriggered: how many times abilities on this card were triggered this turn.
 * - maxAbilities: the maximum number of times abilities on this card may be triggered this turn.
 *   Example: card 47 (drawOne3x) can be triggered up to 3 times; most others are 0 or 1.
 * - fromDrawStack: true if the card entered the present area from the draw stack on this turn,
 *   false if it came from past or future areas. This determines whether takeCandy should be applied.
 */
public record Card(int number, int abilitiesTriggered, int maxAbilities, boolean fromDrawStack) {
    public Card {
        if (number < 1 || number > 48) {
            throw new IllegalArgumentException("Card number must be between 1 and 48, got: " + number);
        }
        if (maxAbilities < 0) {
            throw new IllegalArgumentException("maxAbilities cannot be negative, got: " + maxAbilities);
        }
        if (abilitiesTriggered < 0) {
            throw new IllegalArgumentException("abilitiesTriggered cannot be negative, got: " + abilitiesTriggered);
        }
        if (abilitiesTriggered > maxAbilities) {
            throw new IllegalArgumentException("abilitiesTriggered (" + abilitiesTriggered + ") cannot exceed maxAbilities (" + maxAbilities + ")");
        }
    }

    /**
     * Returns the number of ability uses remaining for this card this turn.
     */
    public int abilityUsesRemaining() {
        return maxAbilities - abilitiesTriggered;
    }

    /**
     * Returns whether this card can still trigger abilities this turn.
     */
    public boolean canTriggerAbility() {
        return abilityUsesRemaining() > 0;
    }

    /**
     * Creates a new card with one more ability triggered.
     */
    public Card withAbilityTriggered() {
        if (!canTriggerAbility()) {
            throw new IllegalStateException("Cannot trigger ability: no uses remaining");
        }
        return new Card(number, abilitiesTriggered + 1, maxAbilities, fromDrawStack);
    }

    /**
     * Creates a new card with the ability triggers reset to 0.
     * Typically used when moving to a new turn.
     */
    public Card withAbilitiesReset() {
        return new Card(number, 0, maxAbilities, fromDrawStack);
    }

    /**
     * Convenience constructor that defaults fromDrawStack to true.
     * Used when creating cards that are known to come from the draw stack.
     */
    public Card(int number, int abilitiesTriggered, int maxAbilities) {
        this(number, abilitiesTriggered, maxAbilities, true);
    }
}