package com.adrian.finished.ui.card;

import com.adrian.finished.model.Card;
import com.adrian.finished.ui.DimensionService;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Interactive version of CardComponent that supports click interactions,
 * candy activation, and drag-and-drop reordering for cards in the Present area.
 */
public class InteractiveCardComponent extends CardComponent {

    private Runnable onCandyActivation;
    private Runnable onClick;
    private CardSwapCallback onCardSwap;

    // Delegate drag handling to separate class
    private final CardDragHandler dragHandler;

    @FunctionalInterface
    public interface CardSwapCallback {
        void onCardSwap(InteractiveCardComponent sourceCard, InteractiveCardComponent targetCard);
    }

    public InteractiveCardComponent(DimensionService dimensionService, Card card) {
        super(dimensionService, card, false); // Interactive cards are always normal size

        // Ensure no background or border styling that could interfere with drag view
        setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;");

        // Create drag handler
        this.dragHandler = new CardDragHandler(this);

        // Set up interaction handlers
        setupInteractions();
    }

    private void setupInteractions() {
        // Add interactive style class
        getStyleClass().add("interactive-card");

        // Handle mouse clicks
        setOnMouseClicked(this::handleMouseClick);

        // Add hover effects (but ensure no border/background)
        setOnMouseEntered(e -> {
            if (!isDisabled()) {
                getStyleClass().add("hover");
            }
        });

        setOnMouseExited(e -> getStyleClass().remove("hover"));

        // Make sure we're not disabled by default (unlike base CardComponent)
        setDisabled(false);
    }

    private void handleMouseClick(MouseEvent event) {
        // Don't handle clicks if a drag is in progress
        if (dragHandler.isDragInProgress()) {
            return;
        }

        if (isDisabled()) {
            return;
        }

        Card card = getCard();
        if (card == null) {
            return;
        }

        // Check for primary button click - use getButton() instead of isPrimaryButtonDown()
        // isPrimaryButtonDown() is false in click events because the button was already released
        if (event.getButton() == MouseButton.PRIMARY) {
            handleCandyActivation();
        }

    }

    private void handleCandyActivation() {
        Card card = getCard();
        if (card == null) return;

        // Use the general approach based on Card model capabilities
        if (hasMultipleCandySlots(card)) {
            // Multi-candy cards: can be activated multiple times based on Card model logic
            if (card.canTriggerAbility()) {
                // Try to activate with candy
                if (tryActivateWithCandy()) {
                    // Notify callback
                    if (onCandyActivation != null) {
                        onCandyActivation.run();
                    }
                }
            }
        } else {
            // Single-candy cards: use the original UI boolean logic
                // Try to activate with candy
                if (tryActivateWithCandy()) {
                    // Notify callback
                    if (onCandyActivation != null) {
                        onCandyActivation.run();
                    }
                }
        }
    }


    // Callback setters
    public void setOnCandyActivation(Runnable callback) {
        this.onCandyActivation = callback;
    }

    public void setOnClick(Runnable callback) {
        this.onClick = callback;
    }

    public void setOnCardSwap(CardSwapCallback callback) {
        this.onCardSwap = callback;
    }

    // Package-private getter for drag handler
    CardSwapCallback getCardSwapCallback() {
        return onCardSwap;
    }


    /**
     * Determines if this card supports multiple candy slots.
     */
    private boolean hasMultipleCandySlots(Card card) {
        return card != null && card.maxAbilities() > 1;
    }
}