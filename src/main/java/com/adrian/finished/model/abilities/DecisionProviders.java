package com.adrian.finished.model.abilities;

import com.adrian.finished.model.GameState;

import java.util.List;

/**
 * Factory/utility for {@link DecisionProvider} instances.
 */
public final class DecisionProviders {
    private static final DecisionProvider NO_OP = new DecisionProvider() {
        @Override
        public List<Integer> selectPresentCardIndices(GameState state, int count) {
            throw new UnsupportedOperationException("No DecisionProvider installed for interactive ability requiring user input.");
        }

        @Override
        public int selectAbilityProviderCard(GameState state, List<Integer> validCardNumbers) {
            throw new UnsupportedOperationException("No DecisionProvider installed for interactive ability requiring user input.");
        }
    };

    private DecisionProviders() { }

    /**
     * Returns a DecisionProvider that throws UnsupportedOperationException for any prompt.
     * Useful as a safe default in non-interactive contexts or tests.
     */
    public static DecisionProvider noOp() {
        return NO_OP;
    }
}