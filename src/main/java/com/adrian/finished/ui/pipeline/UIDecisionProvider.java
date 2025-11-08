package com.adrian.finished.ui.pipeline;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.GameState;
import com.adrian.finished.model.abilities.DecisionProvider;
import com.adrian.finished.ui.DimensionService;
import com.adrian.finished.ui.pipeline.decision.CardSelectionDecisionOverlay;
import com.adrian.finished.ui.pipeline.decision.AbilityProviderDecisionOverlay;
import com.adrian.finished.ui.pipeline.decision.NumberSelectionDecisionOverlay;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * UI-driven DecisionProvider that creates interactive overlays to collect user decisions
 * required by abilities. Blocks ability execution until user provides input.
 *
 * Works identically on desktop JavaFX and JPro web deployment.
 */
public class UIDecisionProvider implements DecisionProvider {

    private final Pane rootPane;
    private final DimensionService dimensionService;

    // Add this field to store the last clicked card index
    private Integer preSelectedAbilityProviderIndex = null;


    // Store pre-selected indices for drag-and-drop operations
    private List<Integer> preSelectedCardIndices = null;

    public UIDecisionProvider(Pane rootPane, DimensionService dimensionService) {
        this.rootPane = rootPane;
        this.dimensionService = dimensionService;
    }

    @Override
    public List<Integer> selectPresentCardIndices(GameState state, int count) {
        // Check if we have pre-selected indices (from drag-and-drop)
        if (preSelectedCardIndices != null && preSelectedCardIndices.size() == count) {
            List<Integer> result = new ArrayList<>(preSelectedCardIndices);
            preSelectedCardIndices = null; // Clear after use
            System.out.println("🎯 Using pre-selected card indices: " + result);
            return result;
        }

        if (count <= 0 || state.present().cards().isEmpty()) {
            return List.of();
        }

        // Create decision overlay for card selection
        CardSelectionDecisionOverlay overlay = new CardSelectionDecisionOverlay(
                dimensionService,
                state.present().cards(),
                count,
                "Select " + count + " card" + (count > 1 ? "s" : "") + " from your present area:"
        );

        return executeDecisionOnFxThread(overlay);
    }

    /**
     * Set pre-selected card indices for the next selectPresentCardIndices call.
     * This is used for drag-and-drop operations where the user has already made the selection.
     */
    public void setPreSelectedCardIndices(List<Integer> indices) {
        this.preSelectedCardIndices = indices != null ? new ArrayList<>(indices) : null;
        System.out.println("🎯 Pre-selected card indices set: " + this.preSelectedCardIndices);
    }

    @Override
    public int selectAbilityProviderCard(GameState state, List<Integer> validCardNumbers) {
        // Check if we have a pre-selected provider (from card click)
        if (preSelectedAbilityProviderIndex != null) {
            int index = preSelectedAbilityProviderIndex;
            preSelectedAbilityProviderIndex = null; // Clear after use

            // Validate that the pre-selected card can actually provide this ability
            if (index >= 0 && index < state.present().cards().size()) {
                Card card = state.present().cards().get(index);
                if (validCardNumbers.contains(card.number()) &&
                        card.abilitiesTriggered() < card.maxAbilities()) {
                    System.out.println("🎯 Using pre-selected ability provider index: " + index);
                    return index;
                }
            }

            System.out.println("⚠️ Pre-selected provider index " + index + " is invalid, showing overlay");
            // Fall through to show overlay if pre-selected card is invalid
        }

        if (validCardNumbers.isEmpty() || state.present().cards().isEmpty()) {
            return -1;
        }

        // Create decision overlay for ability provider selection
        AbilityProviderDecisionOverlay overlay = new AbilityProviderDecisionOverlay(
                dimensionService,
                state.present().cards(),
                validCardNumbers,
                "Select a card to activate its ability:"
        );

        Integer result = executeDecisionOnFxThread(overlay);
        return result != null ? result : -1;
    }


    /**
     * Extended method for number selection (not in base DecisionProvider interface).
     * This demonstrates the extensibility of the UI decision system.
     */
    public int chooseNumber(int min, int max, String prompt) {
        if (min > max) {
            return min;
        }

        // Create decision overlay for number selection
        NumberSelectionDecisionOverlay overlay = new NumberSelectionDecisionOverlay(
                dimensionService,
                min,
                max,
                prompt
        );

        Integer result = executeDecisionOnFxThread(overlay);
        return result != null ? result : min;
    }

    /**
     * Set the pre-selected ability provider card index for the next selectAbilityProviderCard call.
     * This is used when the user has already selected a card by clicking on it.
     */
    public void setPreSelectedAbilityProviderIndex(Integer index) {
        this.preSelectedAbilityProviderIndex = index;
        System.out.println("🎯 Pre-selected ability provider index set: " + this.preSelectedAbilityProviderIndex);
    }


    /**
     * Executes a decision overlay on the JavaFX Application Thread and blocks until complete.
     * This method ensures proper threading for both desktop and JPro web deployment.
     */
    private <T> T executeDecisionOnFxThread(DecisionOverlay<T> overlay) {
        if (Platform.isFxApplicationThread()) {
            // We're already on the FX thread - we need to avoid blocking it
            // For automatic abilities like DRAW_ONE, return immediately with a default result
            // For interactive abilities, we need to restructure this
            CompletableFuture<T> future = new CompletableFuture<>();

            // Add overlay to root pane
            rootPane.getChildren().add(overlay);

            // Set up a listener for when the decision is made
            overlay.setOnDecisionComplete(result -> {
                rootPane.getChildren().remove(overlay);
                future.complete(result);
            });

            // For now, return null to indicate no immediate result
            // This will require changes to how abilities handle decision provider responses
            return null;
        } else {
            CompletableFuture<T> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                try {
                    T result = executeDecisionOverlay(overlay);
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Decision collection was interrupted", e);
            }
        }
    }



    /**
     * Shows the decision overlay and blocks until user makes a decision.
     * Must be called on JavaFX Application Thread.
     */
    private <T> T executeDecisionOverlay(DecisionOverlay<T> overlay) {
        // Add overlay to root pane
        rootPane.getChildren().add(overlay);

        try {
            // Block until decision is made
            return overlay.waitForDecision();
        } finally {
            // Always remove overlay when done
            rootPane.getChildren().remove(overlay);
        }
    }
}
