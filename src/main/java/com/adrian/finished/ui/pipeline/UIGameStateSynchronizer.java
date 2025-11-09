package com.adrian.finished.ui.pipeline;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.GameState;
import com.adrian.finished.ui.layout.*;

import java.util.Deque;
import javafx.application.Platform;
import java.util.List;

/**
 * Synchronizes the UI with the current GameState.
 * Updates all game area layouts when the state changes.
 */
public class UIGameStateSynchronizer {

    private final GameAreasLayout gameAreasLayout;
    private final PresentAreaLayout presentArea;
    private final FutureAreasLayout futureAreas;
    private final PastAreaLayout pastArea;
    private final ActiveStashLayout activeStash;
    private final FinishedPileOverlay finishedPile;

    public UIGameStateSynchronizer(GameAreasLayout gameAreasLayout, FinishedPileOverlay finishedPile) {
        this.gameAreasLayout = gameAreasLayout;
        this.presentArea = gameAreasLayout.getPresentAreaLayout();
        this.futureAreas = gameAreasLayout.getFutureAreasLayout();
        this.pastArea = gameAreasLayout.getPastAreaLayout();
        this.activeStash = gameAreasLayout.getActiveStashLayout();
        this.finishedPile = finishedPile;
    }

    /**
     * Update all UI areas to match the current GameState.
     * Must be called on JavaFX Application Thread.
     */
    public void updateUI(GameState state) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateUI(state));
            return;
        }
        System.out.println("ADRIAN 11000");
        try {
            // Update Present Area
            updatePresentArea(state);

            // Update Past Area
            updatePastArea(state);

            // Update Future Areas
            updateFutureAreas(state);

            // Update Active Stash
            updateActiveStash(state);

            // Update Finished Pile
            updateFinishedPile(state);

            // Handle game end states
            if (state.gameEnd()) {
                handleGameEnd(state);
            }

        } catch (Exception e) {
            System.err.println("Error updating UI from GameState: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the present area with current cards.
     */
    private void updatePresentArea(GameState state) {
        List<Card> presentCards = state.present().cards();
        presentArea.setCards(presentCards);

        if (!presentCards.isEmpty()) {
            System.out.println("📋 Present area updated: " +
                presentCards.stream().map(Card::number).toList());
        }
    }

    /**
     * Update the past area with current cards.
     */
    private void updatePastArea(GameState state) {
        Deque<Card> pastCards = state.past().cards();

        if (!pastCards.isEmpty()) {
            // Convert Deque to List and show all past cards (PastAreaLayout will handle showing max 3)
            List<Card> pastCardsList = List.copyOf(pastCards);
            pastArea.setPastCards(pastCardsList);
            System.out.println("📚 Past area updated: " + pastCards.size() + " cards total, showing up to 3 most recent");
        } else {
            pastArea.setPastCards(List.of()); // Empty list
            System.out.println("📚 Past area cleared - no cards");
        }
    }


    /**
     * Update future areas with current cards.
     */
    private void updateFutureAreas(GameState state) {
        List<com.adrian.finished.model.FutureArea> futureAreasList = state.futureAreas();

        if (futureAreasList.isEmpty()) {
            // No future areas - clear the display
            futureAreas.setFutureCard(null);
            System.out.println("🔮 Future areas cleared - no future areas");
            return;
        }

        // Get cards from the most recent future area (last in the list)
        com.adrian.finished.model.FutureArea mostRecentFutureArea = futureAreasList.getFirst();
        List<Card> mostRecentCards = mostRecentFutureArea.cards();

        if (mostRecentCards.isEmpty()) {
            // Future areas exist but the most recent contains no cards
            futureAreas.setFutureCard(null);
            System.out.println("🔮 Future areas cleared - most recent area contains no cards");
            return;
        }

        // Update the UI with cards from the most recent future area and total area count
        futureAreas.setFutureCards(mostRecentCards, futureAreasList.size());
        System.out.println("🔮 Future areas updated: showing " + mostRecentCards.size() +
                " cards from most recent area (" + futureAreasList.size() + " total future areas): " +
                mostRecentCards.stream().map(Card::number).toList());
    }

    /**
     * Update the active stash display.
     */
    private void updateActiveStash(GameState state) {
        int candyCount = state.activeStash().candy();
        int coffeeCount = state.activeStash().coffee();

        // Update stash display
        activeStash.updateStash(candyCount, coffeeCount);
        System.out.println("💰 Active stash updated: " + candyCount + " candy, " + coffeeCount + " coffee");

        // Enable/disable end turn button based on game state
        // The end turn button should be enabled if:
        // 1. Game is not ended
        // 2. Present area has cards (normal case) OR present area is empty but past area has cards
        //    (This handles the BELOW_THE_STACK case where present is empty but the turn should still be endable)
        boolean canEndTurn = !state.gameEnd() &&
                (!state.present().cards().isEmpty() || !state.past().cards().isEmpty());

        activeStash.setEndTurnEnabled(canEndTurn);
    }


    /**
     * Update the finished pile with scored cards.
     */
    private void updateFinishedPile(GameState state) {
        List<Card> finishedCards = state.finishedPile().cards();

        if (!finishedCards.isEmpty()) {
            // Show the highest scored card
            Card highestCard = finishedCards.stream()
                .max((c1, c2) -> Integer.compare(c1.number(), c2.number()))
                .orElse(null);

            if (highestCard != null) {
                finishedPile.setFinishedCard(highestCard);
                System.out.println("🏆 Finished pile updated: " + finishedCards.size() +
                    " cards, highest is " + highestCard.number());
            }
        } else {
            finishedPile.setFinishedCard(null);
        }
    }

    /**
     * Handle game end states.
     */
    private void handleGameEnd(GameState state) {
        if (state.finishedPile().cards().stream().anyMatch(card -> card.number() == 48)) {
            System.out.println("🎉 VICTORY! Card 48 has been scored!");
            showGameEndMessage("Victory!", "You have successfully completed the game by scoring card 48!");
        } else {
            System.out.println("💀 DEFEAT! Card 48 entered the past area with no coffee tokens.");
            showGameEndMessage("Game Over", "Card 48 entered the past area and you had no coffee tokens to spend!");
        }
    }

    /**
     * Show a game end message to the player.
     */
    private void showGameEndMessage(String title, String message) {
        Platform.runLater(() -> {
            // In a full implementation, this would show a proper dialog
            // For now, just log the message
            System.out.println("🎮 " + title + ": " + message);
        });
    }

    /**
     * Get debugging info about current UI state.
     */
    public String getUIStateInfo() {
        return String.format("UI State - Present: %d cards",
            presentArea.getCurrentCards().size()
//            pastArea.getPastCard() != null ? "Card " + pastArea.getPastCard().number() : "empty",
//            futureAreas.getFutureCard() != null ? "Card " + futureAreas.getFutureCard().number() : "empty",
//            finishedPile.getFinishedCard() != null ? "Card " + finishedPile.getFinishedCard().number() : "empty"
        );
    }
}
