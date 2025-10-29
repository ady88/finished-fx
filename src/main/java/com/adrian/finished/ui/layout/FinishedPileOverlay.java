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

/**
 * Overlay layout for the finished pile, positioned in the bottom-left corner.
 * Shows completed cards in a compact stack format.
 * Sizing and positioning strictly follow DimensionService and global_ui_rules.md.
 */
public class FinishedPileOverlay extends Region {

    private final DimensionService dimensionService;
    private final VBox contentArea;

    public FinishedPileOverlay(DimensionService dimensionService) {
        this.dimensionService = dimensionService;

        // Set CSS style class
        getStyleClass().add("finished-pile-overlay");

        // Create content area
        this.contentArea = new VBox();
        contentArea.setAlignment(Pos.CENTER);
        contentArea.spacingProperty().bind(dimensionService.gapSmallProperty());
        contentArea.paddingProperty().bind(Bindings.createObjectBinding(
            () -> new Insets(dimensionService.gapSmallProperty().get()),
            dimensionService.gapSmallProperty()
        ));

        // Sizing: back to original dimensions (10% width, 20% height)
        prefWidthProperty().bind(dimensionService.finishedPileWidthProperty());
        prefHeightProperty().bind(dimensionService.finishedPileHeightProperty());
        minWidthProperty().bind(dimensionService.finishedPileWidthProperty());
        minHeightProperty().bind(dimensionService.finishedPileHeightProperty());
        maxWidthProperty().bind(dimensionService.finishedPileWidthProperty());
        maxHeightProperty().bind(dimensionService.finishedPileHeightProperty());

        // Initial setup
        Label placeholder = new Label("Finished\nPile");
        placeholder.getStyleClass().add("finished-pile-label");
        contentArea.getChildren().add(placeholder);

        getChildren().add(contentArea);
    }

    public void setFinishedCard(Card card) {
        contentArea.getChildren().clear();

        if (card != null) {
            CardComponent cardComponent = new CardComponent(
                dimensionService,
                card,
                true  // Use small variant
            );

            // Override dimensions to make card even smaller for finished pile
            javafx.beans.binding.DoubleBinding customWidth = prefWidthProperty().multiply(0.9);
            javafx.beans.binding.DoubleBinding customHeight = prefHeightProperty().multiply(0.9);
            cardComponent.overrideDimensions(customWidth, customHeight);

            contentArea.getChildren().add(cardComponent);
        } else {
            Label placeholder = new Label("Finished\nPile");
            placeholder.getStyleClass().add("finished-pile-label");
            contentArea.getChildren().add(placeholder);
        }
    }

    public void setFinishedCards(java.util.List<Card> cards) {
        contentArea.getChildren().clear();

        if (cards == null || cards.isEmpty()) {
            Label placeholder = new Label("Finished\nPile");
            placeholder.getStyleClass().add("finished-pile-label");
            contentArea.getChildren().add(placeholder);
            return;
        }

        Card topCard = cards.get(cards.size() - 1);
        CardComponent cardComponent = new CardComponent(
            dimensionService,
            topCard,
            true
        );

        // Override dimensions to make card even smaller for finished pile
        javafx.beans.binding.DoubleBinding customWidth = prefWidthProperty().multiply(0.9);
        javafx.beans.binding.DoubleBinding customHeight = prefHeightProperty().multiply(0.9);
        cardComponent.overrideDimensions(customWidth, customHeight);

        contentArea.getChildren().add(cardComponent);

        String labelText = cards.size() > 1
            ? "Finished\nPile (" + cards.size() + ")"
            : "Finished\nPile";

        Label label = new Label(labelText);
        label.getStyleClass().add("finished-pile-label");
        contentArea.getChildren().add(label);
    }

    public VBox getContentArea() {
        return contentArea;
    }
}
