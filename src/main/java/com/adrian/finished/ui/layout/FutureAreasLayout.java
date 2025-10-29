package com.adrian.finished.ui.layout;

import com.adrian.finished.model.Card;
import com.adrian.finished.ui.DimensionService;
import com.adrian.finished.ui.card.CardComponent;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Layout for Future Areas, displaying upcoming cards vertically.
 * Handles overflow by scaling or hiding uppermost areas as per global UI rules.
 */
public class FutureAreasLayout extends Region {

    private final DimensionService dimensionService;
    private final VBox contentArea;

    public FutureAreasLayout(DimensionService dimensionService) {
        super();
        this.dimensionService = dimensionService;

        // Set CSS style class
        getStyleClass().add("future-areas-background");

        // Create content area
        this.contentArea = new VBox();
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.spacingProperty().bind(dimensionService.gapSmallProperty());
        contentArea.paddingProperty().bind(Bindings.createObjectBinding(
            () -> new Insets(dimensionService.gapSmallProperty().get()),
            dimensionService.gapSmallProperty()
        ));

        // Initial placeholder
        Label placeholder = new Label("Future Areas - Upcoming cards");
        placeholder.getStyleClass().add("placeholder-text");
        contentArea.getChildren().add(placeholder);

        getChildren().add(contentArea);

        // Bind content area size to this layout's size
        contentArea.prefWidthProperty().bind(widthProperty());
        contentArea.prefHeightProperty().bind(heightProperty());
    }

    public void setFutureCard(Card card) {
        contentArea.getChildren().clear();

        if (card != null) {
            CardComponent cardComponent = new CardComponent(
                dimensionService,
                card,
                true  // Future cards use small variant as per milestone_2.md
            );
            contentArea.getChildren().add(cardComponent);
        } else {
            Label placeholder = new Label("Future Areas - Upcoming cards");
            placeholder.getStyleClass().add("placeholder-text");
            contentArea.getChildren().add(placeholder);
        }
    }

    public void setFutureCards(List<Card> cards) {
        contentArea.getChildren().clear();

        if (cards == null || cards.isEmpty()) {
            Label placeholder = new Label("Future Areas - Upcoming cards");
            placeholder.getStyleClass().add("placeholder-text");
            contentArea.getChildren().add(placeholder);
            return;
        }

        // Add cards with overflow handling
        int maxVisibleCards = calculateMaxVisibleCards();
        int cardsToShow = Math.min(cards.size(), maxVisibleCards);

        // Add overflow indicator if needed
        if (cards.size() > maxVisibleCards) {
            Label indicator = new Label("+" + (cards.size() - maxVisibleCards) + " more");
            indicator.getStyleClass().add("future-overflow-indicator");
            contentArea.getChildren().add(indicator);
        }

        // Add visible cards
        for (int i = 0; i < cardsToShow; i++) {
            CardComponent cardComponent = new CardComponent(
                dimensionService,
                cards.get(i),
                true  // Future cards use small variant as per milestone_2.md
            );
            contentArea.getChildren().add(cardComponent);
        }
    }

    private int calculateMaxVisibleCards() {
        // Calculate how many cards can fit vertically without scrolling
        // This would be based on available height and card height
        // For now, return a reasonable default
        return 3;
    }

    public VBox getContentArea() {
        return contentArea;
    }
}
