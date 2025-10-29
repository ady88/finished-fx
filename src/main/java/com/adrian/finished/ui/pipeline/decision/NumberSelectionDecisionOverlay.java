package com.adrian.finished.ui.pipeline.decision;

import com.adrian.finished.ui.DimensionService;
import com.adrian.finished.ui.pipeline.DecisionOverlay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

/**
 * Decision overlay for selecting a number within a specified range.
 * Provides an intuitive slider interface with direct value feedback.
 */
public class NumberSelectionDecisionOverlay extends DecisionOverlay<Integer> {

    private final DimensionService dimensionService;
    private final int minValue;
    private final int maxValue;
    private final String promptText;

    private Slider valueSlider;
    private Label valueLabel;
    private Label statusLabel;
    private Button confirmButton;
    private Button cancelButton;

    public NumberSelectionDecisionOverlay(DimensionService dimensionService,
                                         int minValue,
                                         int maxValue,
                                         String promptText) {
        this.dimensionService = dimensionService;
        this.minValue = minValue;
        this.maxValue = maxValue;
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

        // Create value display
        VBox valueDisplay = createValueDisplay();

        // Create status label
        statusLabel = new Label();
        statusLabel.getStyleClass().add("decision-status");
        statusLabel.fontProperty().bind(
            dimensionService.baseFontSizeProperty()
                .asObject()
                .map(size -> Font.font(size.doubleValue()))
        );

        // Create button area
        HBox buttonArea = createButtonArea();

        // Add all components to main container
        mainContainer.getChildren().addAll(
            promptLabel,
            valueDisplay,
            statusLabel,
            buttonArea
        );

        // Center everything in the overlay
        getChildren().add(mainContainer);
    }

    private VBox createValueDisplay() {
        VBox valueDisplay = new VBox();
        valueDisplay.setAlignment(Pos.CENTER);
        valueDisplay.spacingProperty().bind(dimensionService.gapSmallProperty());

        // Create value label
        valueLabel = new Label(String.valueOf(minValue));
        valueLabel.getStyleClass().add("number-value-label");
        valueLabel.fontProperty().bind(
            dimensionService.baseFontSizeProperty()
                .asObject()
                .map(size -> Font.font(size.doubleValue() * 2))
        );

        // Create slider
        valueSlider = new Slider(minValue, maxValue, minValue);
        valueSlider.getStyleClass().add("number-value-slider");
        valueSlider.setShowTickLabels(true);
        valueSlider.setShowTickMarks(true);
        valueSlider.setMajorTickUnit((maxValue - minValue) / 4.0);
        valueSlider.setMinorTickCount(0);
        valueSlider.setSnapToTicks(true);

        // Bind slider width to dimension service
        valueSlider.prefWidthProperty().bind(
            dimensionService.sceneWidthProperty().multiply(0.3)
        );

        // Create range label
        Label rangeLabel = new Label(String.format("Range: %d - %d", minValue, maxValue));
        rangeLabel.getStyleClass().add("number-range-label");
        rangeLabel.fontProperty().bind(
            dimensionService.smallFontSizeProperty()
                .asObject()
                .map(size -> Font.font(size.doubleValue()))
        );

        valueDisplay.getChildren().addAll(valueLabel, valueSlider, rangeLabel);
        return valueDisplay;
    }

    private HBox createButtonArea() {
        HBox buttonArea = new HBox();
        buttonArea.getStyleClass().add("decision-buttons");
        buttonArea.setAlignment(Pos.CENTER);

        // Bind spacing to dimension service
        buttonArea.spacingProperty().bind(dimensionService.gapMediumProperty());

        // Create confirm button
        confirmButton = new Button("Confirm");
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
        // Update value label when slider changes
        valueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int intValue = newValue.intValue();
            valueLabel.setText(String.valueOf(intValue));
            updateUI();
        });

        confirmButton.setOnAction(event -> {
            completeDecision((int) valueSlider.getValue());
        });

        cancelButton.setOnAction(event -> cancelDecision());

        // ESC key to cancel
        setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE -> cancelDecision();
                case ENTER -> completeDecision((int) valueSlider.getValue());
            }
        });

        // Focus this overlay so it can receive key events
        setFocusTraversable(true);
        requestFocus();
    }

    private void updateUI() {
        int currentValue = (int) valueSlider.getValue();
        statusLabel.setText("Selected value: " + currentValue);
    }
}
