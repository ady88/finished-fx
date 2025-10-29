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
 * Decision overlay for selecting which card should provide an ability activation.
 * Shows only cards that can provide the requested ability and haven't exceeded
 * their usage limits.
 */
public class AbilityProviderDecisionOverlay extends DecisionOverlay<Integer> {

    private final DimensionService dimensionService;
    private final List<Card> presentCards;
    private final List<Integer> validCardNumbers;
    private final String promptText;

    private final List<InteractiveCardComponent> validCardComponents = new ArrayList<>();
    private final List<Integer> validCardIndices = new ArrayList<>();
    private Integer selectedIndex = null;
    private Label statusLabel;
    private Button confirmButton;
    private Button cancelButton;

    public AbilityProviderDecisionOverlay(DimensionService dimensionService,
                                         List<Card> presentCards,
                                         List<Integer> validCardNumbers,
                                         String promptText) {
        this.dimensionService = dimensionService;
        this.presentCards = List.copyOf(presentCards);
        this.validCardNumbers = List.copyOf(validCardNumbers);
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

        // Find valid cards and create components
        for (int i = 0; i < presentCards.size(); i++) {
            Card card = presentCards.get(i);

            // Check if this card number is valid for the ability
            if (validCardNumbers.contains(card.number())) {
                InteractiveCardComponent cardComponent = new InteractiveCardComponent(dimensionService, card);

                // Add ability provider styling
                cardComponent.getStyleClass().add("ability-provider-card");

                // Store the present area index for this valid card
                final int presentAreaIndex = i;
                validCardIndices.add(presentAreaIndex);

                // Set up click handler for selection
                cardComponent.setOnMouseClicked(event -> selectCard(presentAreaIndex));

                validCardComponents.add(cardComponent);
                cardArea.getChildren().add(cardComponent);
            }
        }

        // If no valid cards, show message
        if (validCardComponents.isEmpty()) {
            Label noCardsLabel = new Label("No cards available for this ability");
            noCardsLabel.getStyleClass().add("no-cards-message");
            noCardsLabel.fontProperty().bind(
                dimensionService.baseFontSizeProperty()
                    .asObject()
                    .map(size -> Font.font(size.doubleValue()))
            );
            cardArea.getChildren().add(noCardsLabel);
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
        confirmButton = new Button("Activate Ability");
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
            if (selectedIndex != null) {
                completeDecision(selectedIndex);
            }
        });

        cancelButton.setOnAction(event -> cancelDecision());

        // ESC key to cancel
        setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE -> cancelDecision();
                case ENTER -> {
                    if (selectedIndex != null) {
                        completeDecision(selectedIndex);
                    }
                }
            }
        });

        // Focus this overlay so it can receive key events
        setFocusTraversable(true);
        requestFocus();
    }

    private void selectCard(int presentAreaIndex) {
        // Clear previous selection
        clearSelection();

        // Set new selection
        selectedIndex = presentAreaIndex;

        // Find and highlight the selected card component
        int validComponentIndex = validCardIndices.indexOf(presentAreaIndex);
        if (validComponentIndex >= 0) {
            InteractiveCardComponent selectedComponent = validCardComponents.get(validComponentIndex);
            selectedComponent.getStyleClass().add("selected-card");
        }

        updateUI();
    }

    private void clearSelection() {
        selectedIndex = null;

        // Remove selection styling from all cards
        for (InteractiveCardComponent card : validCardComponents) {
            card.getStyleClass().remove("selected-card");
        }
    }

    private void updateUI() {
        if (validCardComponents.isEmpty()) {
            statusLabel.setText("No valid cards available");
            confirmButton.setDisable(true);
        } else if (selectedIndex == null) {
            statusLabel.setText("Select a card to activate its ability");
            confirmButton.setDisable(true);
        } else {
            Card selectedCard = presentCards.get(selectedIndex);
            statusLabel.setText(String.format("Activate ability on card %d", selectedCard.number()));
            confirmButton.setDisable(false);
        }
    }
}
