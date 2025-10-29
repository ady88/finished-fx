package com.adrian.finished.ui.pipeline.decision;

import com.adrian.finished.model.Card;
import com.adrian.finished.ui.DimensionService;
import com.adrian.finished.ui.card.InteractiveCardComponent;
import com.adrian.finished.ui.pipeline.DecisionOverlay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * Decision overlay for selecting cards from the present area.
 * Displays available cards with interactive selection and enforces
 * the required selection count constraints.
 */
public class CardSelectionDecisionOverlay extends DecisionOverlay<List<Integer>> {

    private final DimensionService dimensionService;
    private final List<Card> availableCards;
    private final int requiredCount;
    private final String promptText;

    private final List<Integer> selectedIndices = new ArrayList<>();
    private final List<InteractiveCardComponent> cardComponents = new ArrayList<>();
    private Label statusLabel;
    private Button confirmButton;
    private Button cancelButton;

    public CardSelectionDecisionOverlay(DimensionService dimensionService,
                                       List<Card> availableCards,
                                       int requiredCount,
                                       String promptText) {
        this.dimensionService = dimensionService;
        this.availableCards = List.copyOf(availableCards);
        this.requiredCount = requiredCount;
        this.promptText = promptText;

        initializeUI();
        setupEventHandlers();
        updateUI();
    }

    private void initializeUI() {
        // Apply decision overlay style class
        getStyleClass().add("decision-overlay");

        // Create main container
        VBox mainContainer = new VBox();
        mainContainer.getStyleClass().add("decision-container");
        mainContainer.setAlignment(Pos.CENTER);

        // Bind spacing to dimension service
        mainContainer.spacingProperty().bind(dimensionService.gapMediumProperty());
        mainContainer.paddingProperty().bind(
            dimensionService.gapLargeProperty().multiply(2)
                .asObject()
                .map(gap -> new Insets(gap.doubleValue()))
        );

        // Create prompt label
        Label promptLabel = new Label(promptText);
        promptLabel.getStyleClass().add("decision-prompt");
        promptLabel.fontProperty().bind(
            dimensionService.baseFontSizeProperty()
                .asObject()
                .map(size -> Font.font(size.doubleValue() * 1.2))
        );

        // Create status label
        statusLabel = new Label();
        statusLabel.getStyleClass().add("decision-status");
        statusLabel.fontProperty().bind(
            dimensionService.baseFontSizeProperty()
                .asObject()
                .map(size -> Font.font(size.doubleValue()))
        );

        // Create card selection area
        HBox cardArea = createCardSelectionArea();

        // Create button area
        HBox buttonArea = createButtonArea();

        // Add all components to main container
        mainContainer.getChildren().addAll(
            promptLabel,
            statusLabel,
            cardArea,
            buttonArea
        );

        // Center everything in the overlay
        getChildren().add(mainContainer);
    }

    private HBox createCardSelectionArea() {
        HBox cardArea = new HBox();
        cardArea.getStyleClass().add("card-selection-area");
        cardArea.setAlignment(Pos.CENTER);

        // Bind spacing to dimension service
        cardArea.spacingProperty().bind(dimensionService.gapMediumProperty());

        // Create interactive card components
        for (int i = 0; i < availableCards.size(); i++) {
            Card card = availableCards.get(i);
            InteractiveCardComponent cardComponent = new InteractiveCardComponent(dimensionService, card);

            // Add selection styling
            cardComponent.getStyleClass().add("selectable-card");

            // Store index for selection tracking
            final int cardIndex = i;

            // Set up click handler for selection
            cardComponent.setOnMouseClicked(event -> toggleCardSelection(cardIndex));

            cardComponents.add(cardComponent);
            cardArea.getChildren().add(cardComponent);
        }

        return cardArea;
    }

    private HBox createButtonArea() {
        HBox buttonArea = new HBox();
        buttonArea.getStyleClass().add("decision-buttons");
        buttonArea.setAlignment(Pos.CENTER);

        // Bind spacing to dimension service
        buttonArea.spacingProperty().bind(dimensionService.gapMediumProperty());

        // Create confirm button
        confirmButton = new Button("Confirm Selection");
        confirmButton.getStyleClass().add("decision-confirm-button");
        confirmButton.fontProperty().bind(
            dimensionService.baseFontSizeProperty()
                .asObject()
                .map(size -> Font.font(size.doubleValue()))
        );

        // Create cancel button
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("decision-cancel-button");
        cancelButton.fontProperty().bind(
            dimensionService.baseFontSizeProperty()
                .asObject()
                .map(size -> Font.font(size.doubleValue()))
        );

        buttonArea.getChildren().addAll(confirmButton, cancelButton);
        return buttonArea;
    }

    private void setupEventHandlers() {
        confirmButton.setOnAction(event -> {
            if (selectedIndices.size() == requiredCount) {
                completeDecision(new ArrayList<>(selectedIndices));
            }
        });

        cancelButton.setOnAction(event -> cancelDecision());

        // ESC key to cancel
        setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE -> cancelDecision();
                case ENTER -> {
                    if (selectedIndices.size() == requiredCount) {
                        completeDecision(new ArrayList<>(selectedIndices));
                    }
                }
            }
        });

        // Focus this overlay so it can receive key events
        setFocusTraversable(true);
        requestFocus();
    }

    private void toggleCardSelection(int cardIndex) {
        InteractiveCardComponent cardComponent = cardComponents.get(cardIndex);

        if (selectedIndices.contains(cardIndex)) {
            // Deselect card
            selectedIndices.remove(Integer.valueOf(cardIndex));
            cardComponent.getStyleClass().remove("selected-card");
        } else {
            // Check if we can select more cards
            if (selectedIndices.size() < requiredCount) {
                selectedIndices.add(cardIndex);
                cardComponent.getStyleClass().add("selected-card");
            }
        }

        updateUI();
    }

    private void updateUI() {
        int selected = selectedIndices.size();
        int remaining = requiredCount - selected;

        if (remaining > 0) {
            statusLabel.setText(String.format("Select %d more card%s", remaining, remaining > 1 ? "s" : ""));
            confirmButton.setDisable(true);
        } else {
            statusLabel.setText("Selection complete - click Confirm or press Enter");
            confirmButton.setDisable(false);
        }

        // Update visual feedback for selectable cards
        for (int i = 0; i < cardComponents.size(); i++) {
            InteractiveCardComponent card = cardComponents.get(i);
            boolean isSelected = selectedIndices.contains(i);
            boolean canSelect = !isSelected && selectedIndices.size() < requiredCount;

            card.setDisable(!canSelect && !isSelected);
        }
    }
}
