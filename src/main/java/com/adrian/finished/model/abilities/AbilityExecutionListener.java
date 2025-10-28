package com.adrian.finished.model.abilities;

/**
 * Listener for UI (or logging) layers to react after an ability was executed.
 * Implementations can update views (e.g., Console, JavaFX) when an ability finishes.
 */
@FunctionalInterface
public interface AbilityExecutionListener {
    /**
     * Called after an ability executor produced a new GameState.
     */
    void afterAbility(AbilityExecutedEvent event);
}
