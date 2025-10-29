package com.adrian.finished.ui.layout;

import com.adrian.finished.model.Card;
import com.adrian.finished.ui.DimensionService;
import com.adrian.finished.ui.card.CardComponent;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * Layout component for the Past Area, showing the last 3 cards that were played.
 * Cards are displayed from left to right in chronological order.
 */
public class PastAreaLayout extends Region {

    private final DimensionService dimensionService;
    private final HBox contentArea;

    public PastAreaLayout(DimensionService dimensionService) {
        super();
        this.dimensionService = dimensionService;

        System.out.println("PastAreaLayout constructor called");

        // Set CSS style class
        getStyleClass().add("past-area-background");

        // Create content area
        this.contentArea = new HBox();
        contentArea.setAlignment(Pos.CENTER);
        contentArea.spacingProperty().bind(dimensionService.gapSmallProperty());
        contentArea.paddingProperty().bind(Bindings.createObjectBinding(
            () -> new Insets(dimensionService.gapMediumProperty().get()),
            dimensionService.gapMediumProperty()
        ));

        // Initial placeholder text
        Label placeholder = new Label("Past Area - Last 3 cards");
        placeholder.getStyleClass().add("placeholder-text");

        contentArea.getChildren().add(placeholder);
        getChildren().add(contentArea);

        // Bind content area size to this layout's size
        contentArea.prefWidthProperty().bind(widthProperty());
        contentArea.prefHeightProperty().bind(heightProperty());

        // Ensure minimum size
        setMinHeight(50);  // Minimum height so we can see the area
        contentArea.setMinHeight(50);

        System.out.println("PastAreaLayout content area setup complete");

        // Add listeners to debug sizing
        widthProperty().addListener((obs, oldVal, newVal) ->
            System.out.println("PastAreaLayout width changed to: " + newVal));
        heightProperty().addListener((obs, oldVal, newVal) ->
            System.out.println("PastAreaLayout height changed to: " + newVal));
    }

    public HBox getContentArea() {
        return contentArea;
    }

    public void setPastCards(List<Card> pastCards) {
        contentArea.getChildren().clear();
        System.out.println("PastAreaLayout.setPastCards called with " + (pastCards != null ? pastCards.size() : "null") + " cards");

        if (pastCards == null || pastCards.isEmpty()) {
            updateDisplay();
            return;
        }

        // Show maximum 3 cards (most recent)
        int cardsToShow = Math.min(3, pastCards.size());
        int startIndex = Math.max(0, pastCards.size() - cardsToShow);

        // Add indicator for hidden cards if there are more than 3
        if (pastCards.size() > 3) {
            Label indicator = new Label("+" + (pastCards.size() - 3) + " more");
            indicator.getStyleClass().add("past-indicator");
            contentArea.getChildren().add(indicator);
        }

        // Add card components for visible cards
        for (int i = startIndex; i < pastCards.size(); i++) {
            Card card = pastCards.get(i);
            System.out.println("Creating CardComponent for card " + card.number() + " in past area");

            CardComponent cardComponent = new CardComponent(
                dimensionService,
                card,
                true  // Cards in past area use small variant (non-interactive)
            );
            contentArea.getChildren().add(cardComponent);

            System.out.println("Added card " + card.number() + " to past area content. Content area now has " + contentArea.getChildren().size() + " children");
        }
    }

    public void setPastCard(Card card) {
        setPastCards(card != null ? List.of(card) : List.of());
    }

    private void updateDisplay() {
        // Method for updating display when no cards are present
        System.out.println("PastAreaLayout.updateDisplay called - showing placeholder");

        Label placeholder = new Label("Past Area - Last 3 cards");
        placeholder.getStyleClass().add("placeholder-text");
        contentArea.getChildren().add(placeholder);
    }
}
