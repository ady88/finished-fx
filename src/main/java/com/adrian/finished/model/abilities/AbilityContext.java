package com.adrian.finished.model.abilities;

import com.adrian.finished.model.GameState;

import java.util.Objects;

/**
 * Minimal immutable context provided to an ability when evaluated/executed.
 * This is a read-only view holding the current GameState plus the ability specification (enum)
 * and a DecisionProvider to synchronously obtain user choices for interactive abilities.
 */
public record AbilityContext(GameState state, AbilitySpec ability, DecisionProvider provider) {
    public AbilityContext {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(provider, "provider");
    }

    /**
     * Convenience constructor that uses a no-op DecisionProvider. Any attempt to request
     * a decision will result in an UnsupportedOperationException. Useful for non-interactive
     * abilities and tests that don't require prompting.
     */
    public AbilityContext(GameState state, AbilitySpec ability) {
        this(state, ability, DecisionProviders.noOp());
    }
}