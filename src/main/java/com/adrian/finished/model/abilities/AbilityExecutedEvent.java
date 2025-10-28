package com.adrian.finished.model.abilities;

import com.adrian.finished.model.GameState;

import java.util.Objects;

/**
 * Immutable event published after an AbilityExecutor is applied.
 * It contains the executed ability and the game state before and after the execution.
 */
public record AbilityExecutedEvent(
        AbilitySpec ability,
        GameState before,
        GameState after
) {
    public AbilityExecutedEvent {
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(before, "before");
        Objects.requireNonNull(after, "after");
    }
}
