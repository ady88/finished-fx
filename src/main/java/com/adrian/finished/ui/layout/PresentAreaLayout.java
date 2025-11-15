package com.adrian.finished.ui.layout;

import com.adrian.finished.model.Card;
import com.adrian.finished.ui.DimensionService;
import com.adrian.finished.ui.card.InteractiveCardComponent;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Layout for the Present Area where players can interact with cards.
 * Displays cards horizontally and handles candy activation interactions
 * and drag-and-drop reordering.
 */
public class PresentAreaLayout extends Region {

    private final DimensionService dimensionService;
    private final HBox contentArea;
    private CandyActivationCallback candyActivationCallback;
    private final List<InteractiveCardComponent> cardComponents = new ArrayList<>();
    private CardSwapCallback cardSwapCallback;


    @FunctionalInterface
    public interface CandyActivationCallback {
        void onCandyActivation(InteractiveCardComponent cardComponent);
    }

    // Add this interface (if not already present)
    @FunctionalInterface
    public interface CardSwapCallback {
        void onCardSwap(InteractiveCardComponent sourceCard, InteractiveCardComponent targetCard);
    }

    // Add this setter method
    public void setCardSwapCallback(CardSwapCallback callback) {
        this.cardSwapCallback = callback;
    }



    public PresentAreaLayout(DimensionService dimensionService) {
        super();
        this.dimensionService = dimensionService;

        // Set CSS style class
        getStyleClass().add("present-area-background");

        // Create content area
        this.contentArea = new HBox();
        contentArea.setAlignment(Pos.CENTER);
        contentArea.spacingProperty().bind(dimensionService.gapMediumProperty());
        contentArea.paddingProperty().bind(Bindings.createObjectBinding(
            () -> new Insets(dimensionService.gapMediumProperty().get()),
            dimensionService.gapMediumProperty()
        ));

        // Initial placeholder
        Label placeholder = new Label("Present Area - Active cards");
        placeholder.getStyleClass().add("placeholder-text");
        contentArea.getChildren().add(placeholder);

        getChildren().add(contentArea);

        // Bind content area size to this layout's size
        contentArea.prefWidthProperty().bind(widthProperty());
        contentArea.prefHeightProperty().bind(heightProperty());
    }

    public void setCandyActivationCallback(CandyActivationCallback callback) {
        this.candyActivationCallback = callback;
    }

    public void addCard(Card card) {
        contentArea.getChildren().clear();
        cardComponents.clear();

        if (card != null) {
            InteractiveCardComponent cardComponent = createInteractiveCard(card);
            cardComponents.add(cardComponent);
            contentArea.getChildren().add(cardComponent);
        }
    }

    public void setCards(List<Card> cards) {
        contentArea.getChildren().clear();
        cardComponents.clear();

        if (cards == null || cards.isEmpty()) {
            Label placeholder = new Label("Present Area - Active cards");
            placeholder.getStyleClass().add("placeholder-text");
            contentArea.getChildren().add(placeholder);
            return;
        }

        // Add all cards with proper scaling if needed
        for (Card card : cards) {
            InteractiveCardComponent cardComponent = createInteractiveCard(card);
            cardComponents.add(cardComponent);
            contentArea.getChildren().add(cardComponent);
        }

        // Scale cards if too many to fit (following global UI rules)
        scaleCardsIfNeeded();
    }

    private InteractiveCardComponent createInteractiveCard(Card card) {
        InteractiveCardComponent cardComponent = new InteractiveCardComponent(
                dimensionService,
                card
        );

        // Set up candy activation handling
        if (candyActivationCallback != null) {
            cardComponent.setOnCandyActivation(() ->
                    candyActivationCallback.onCandyActivation(cardComponent)
            );
        }

        // Set up card swap handling
        cardComponent.setOnCardSwap(this::handleCardSwap);

        return cardComponent;
    }


    // Modify the existing handleCardSwap method
    private void handleCardSwap(InteractiveCardComponent sourceCard, InteractiveCardComponent targetCard) {
        // Find indices of the cards
        int sourceIndex = cardComponents.indexOf(sourceCard);
        int targetIndex = cardComponents.indexOf(targetCard);

        if (sourceIndex == -1 || targetIndex == -1 || sourceIndex == targetIndex) {
            return; // Invalid swap
        }

        System.out.println("UI: Attempting to swap cards at positions " + sourceIndex + " and " + targetIndex);

        // First, notify the game controller to execute the ability
        if (cardSwapCallback != null) {
            cardSwapCallback.onCardSwap(sourceCard, targetCard);
        } else {
            // Fallback: perform UI-only swap if no game loop integration
            System.out.println("No card swap callback set - performing UI-only swap");
            performUISwap(sourceIndex, targetIndex);
        }
    }

    private void performUISwap(int sourceIndex, int targetIndex) {
        // Swap in the card components list
        Collections.swap(cardComponents, sourceIndex, targetIndex);

        // Update the visual layout
        updateVisualLayout();

        System.out.println("UI: Swapped cards at positions " + sourceIndex + " and " + targetIndex);
    }



    private void updateVisualLayout() {
        // Clear and re-add all cards in the new order
        contentArea.getChildren().clear();

        for (InteractiveCardComponent cardComponent : cardComponents) {
            contentArea.getChildren().add(cardComponent);
        }
    }

    /**
     * Get the current cards in their display order
     */
    public List<Card> getCurrentCards() {
        return cardComponents.stream()
            .map(InteractiveCardComponent::getCard)
            .toList();
    }

    private void scaleCardsIfNeeded() {
        // Implementation would scale cards down if they don't fit horizontally
        // Following the global UI rule: no horizontal scrolling, scale to fit
        // This would be implemented based on actual card widths and container width
    }

    public HBox getContentArea() {
        return contentArea;
    }
}
