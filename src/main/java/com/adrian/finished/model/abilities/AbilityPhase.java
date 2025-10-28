package com.adrian.finished.model.abilities;

/**
 * Phases/types under which abilities trigger, as described in game-abilities.json.
 */
public enum AbilityPhase {
    GAME_START,
    TURN_START,
    USER_INPUT_REQUIRED,
    TURN_END,
    GAME_END_VICTORY,
    GAME_END_LOSE
}