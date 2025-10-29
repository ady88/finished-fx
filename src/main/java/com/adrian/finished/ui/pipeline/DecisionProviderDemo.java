package com.adrian.finished.ui.pipeline;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.GameState;
import com.adrian.finished.ui.controller.GameController;
import java.util.ArrayDeque;
import javafx.application.Platform;

import java.util.List;

/**
 * Demonstration utility for testing UIDecisionProvider functionality.
 * Shows various decision overlay scenarios that would be triggered by abilities.
 */
public class DecisionProviderDemo {

    private final GameController gameController;
    private final UIDecisionProvider decisionProvider;

    public DecisionProviderDemo(GameController gameController) {
        this.gameController = gameController;
        this.decisionProvider = gameController.getDecisionProvider();
    }

    /**
     * Demonstrates card selection for CARDS_INTO_PAST ability.
     * This would be triggered when player activates ability on cards 5, 11, 17, 23, 25, or 41.
     */
    public void demonstrateCardSelection() {
        Platform.runLater(() -> {
            System.out.println("=== CARDS_INTO_PAST Ability Demo ===");

            // Create mock GameState with present cards
            List<Card> presentCards = List.of(
                new Card(5, 1, 1, true),   // Card 5 with 1 candy (can activate ability)
                new Card(18, 0, 1, true),  // Card 18 with no candy
                new Card(31, 2, 0, true)   // Card 31 with 2 candy
            );

            GameState mockState = createMockGameState(presentCards);

            try {
                // Simulate CARDS_INTO_PAST ability decision
                System.out.println("Player activates CARDS_INTO_PAST ability - select 2 cards to move to past:");
                List<Integer> selectedIndices = decisionProvider.selectPresentCardIndices(mockState, 2);

                if (selectedIndices != null && selectedIndices.size() == 2) {
                    System.out.println("✅ User selected cards at indices: " + selectedIndices);
                    for (Integer index : selectedIndices) {
                        Card selectedCard = presentCards.get(index);
                        System.out.println("   → Card " + selectedCard.number() + " will be moved to past");
                    }
                    System.out.println("   → 2 new cards will be drawn to replace them");
                } else {
                    System.out.println("❌ User cancelled or made invalid selection");
                }

            } catch (Exception e) {
                System.err.println("❌ Error during card selection: " + e.getMessage());
            }
        });
    }

    /**
     * Demonstrates ability provider selection for DRAW_TWO ability.
     * This would be triggered when multiple cards can provide the same ability.
     */
    public void demonstrateAbilityProviderSelection() {
        Platform.runLater(() -> {
            System.out.println("\n=== DRAW_TWO Ability Provider Demo ===");

            // Create mock GameState with cards that can provide DRAW_TWO
            List<Card> presentCards = List.of(
                new Card(2, 1, 1, true),   // Card 2 can provide DRAW_TWO
                new Card(18, 0, 1, true),  // Card 18 cannot provide DRAW_TWO
                new Card(2, 0, 1, true)    // Another Card 2 that cannot provide (no candy)
            );

            GameState mockState = createMockGameState(presentCards);

            try {
                // Simulate ability provider selection
                System.out.println("Player wants to use DRAW_TWO ability - select which card to activate:");
                List<Integer> validProviders = List.of(2); // Only card number 2 can provide this
                int selectedProvider = decisionProvider.selectAbilityProviderCard(mockState, validProviders);

                if (selectedProvider >= 0) {
                    Card providerCard = presentCards.get(selectedProvider);
                    System.out.println("✅ User selected card " + providerCard.number() + " at index " + selectedProvider);
                    System.out.println("   → Ability will be activated using this card's candy");
                    System.out.println("   → 2 additional cards will be drawn");
                } else {
                    System.out.println("❌ User cancelled ability activation");
                }

            } catch (Exception e) {
                System.err.println("❌ Error during ability provider selection: " + e.getMessage());
            }
        });
    }

    /**
     * Demonstrates number selection for extended decision functionality.
     * This shows the flexibility of the decision system for future abilities.
     */
    public void demonstrateNumberSelection() {
        Platform.runLater(() -> {
            System.out.println("\n=== Number Selection Demo ===");

            try {
                // Simulate number selection (e.g., for DRAW_ONE_3X ability)
                System.out.println("Player activates DRAW_ONE_3X ability - choose how many times to activate (1-3):");
                int selectedCount = decisionProvider.chooseNumber(1, 3, "How many cards do you want to draw?");

                System.out.println("✅ User selected to draw " + selectedCount + " card(s)");
                System.out.println("   → " + selectedCount + " candy will be spent");
                System.out.println("   → " + selectedCount + " card(s) will be drawn");

            } catch (Exception e) {
                System.err.println("❌ Error during number selection: " + e.getMessage());
            }
        });
    }

    /**
     * Runs all demonstration scenarios in sequence.
     */
    public void runAllDemonstrations() {
        System.out.println("🎮 Starting UIDecisionProvider Demonstration Suite");
        System.out.println("Each demo will show a different type of decision overlay...\n");

        // Run demonstrations with delays to show each one clearly
        demonstrateCardSelection();

        Platform.runLater(() -> {
            try {
                Thread.sleep(1000); // Brief pause between demos
                demonstrateAbilityProviderSelection();

                Platform.runLater(() -> {
                    try {
                        Thread.sleep(1000);
                        demonstrateNumberSelection();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private GameState createMockGameState(List<Card> presentCards) {
        return new GameState(
            new com.adrian.finished.model.Stash(5, 3), // activeStash - plenty of candy and coffee
            new com.adrian.finished.model.Stash(10, 5), // reservedStash
            new com.adrian.finished.model.DrawStack(new ArrayDeque<>()), // empty drawStack for demo
            new com.adrian.finished.model.PresentArea(presentCards), // present cards
            new com.adrian.finished.model.PastArea(new ArrayDeque<>()), // empty past
            List.of(), // empty futureAreas
            new com.adrian.finished.model.FinishedPile(List.of()), // empty finishedPile
            0, // activeAllCardsInFutureAreas
            false // gameEnd
        );
    }
}
