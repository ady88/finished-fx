package com.adrian.finished.ui.layout;

import com.adrian.finished.ui.DimensionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Active Stash Bar layout (6% of screen height).
 * Horizontal bar with candy tokens, coffee display, and end turn button.
 * Light orange background for visual distinction during development.
 */
public class ActiveStashLayout extends Region {

    private final DimensionService dimensionService;
    private final HBox contentArea;
    private final HBox candyTokensContainer;
    private final HBox coffeeContainer;
    private final Button endTurnButton;

    // State
    private int activeCandyCount = 5; // Start with 5 candy tokens as per game rules
    private int coffeeCount = 7; // Start with 7 coffee tokens as per game rules

    // Assets
    private Image candyImage;
    private Image candyOutlineImage;
    private Image coffeeImage;

    public ActiveStashLayout(DimensionService dimensionService) {
        this.dimensionService = dimensionService;

        // Light orange background for visual debugging
        getStyleClass().add("active-stash-background");

        // Load assets
        loadAssets();

        // Create main content container
        this.contentArea = new HBox();
        contentArea.setAlignment(Pos.CENTER_LEFT);
        contentArea.spacingProperty().bind(dimensionService.gapLargeProperty());
        contentArea.paddingProperty().bind(
            javafx.beans.binding.Bindings.createObjectBinding(
                () -> new Insets(dimensionService.gapSmallProperty().get()),
                dimensionService.gapSmallProperty()
            )
        );

        // Create candy tokens container (10 slots)
        this.candyTokensContainer = createCandyTokensContainer();

        // Create coffee display
        this.coffeeContainer = createCoffeeContainer();

        // Create end turn button
        this.endTurnButton = createEndTurnButton();

        // Add components to content area
        contentArea.getChildren().addAll(candyTokensContainer, coffeeContainer, endTurnButton);
        getChildren().add(contentArea);

        // Bind content area size
        contentArea.prefWidthProperty().bind(widthProperty());
        contentArea.prefHeightProperty().bind(heightProperty());
    }

    private void loadAssets() {
        try {
            this.candyImage = new Image(getClass().getResourceAsStream("/assets/other/candy.png"));
            this.candyOutlineImage = new Image(getClass().getResourceAsStream("/assets/other/candy-outline.png"));
            this.coffeeImage = new Image(getClass().getResourceAsStream("/assets/other/coffee.png"));
        } catch (Exception e) {
            System.err.println("Warning: Could not load token assets: " + e.getMessage());
            // Assets will be null, ImageViews will handle gracefully
        }
    }

    private HBox createCandyTokensContainer() {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.spacingProperty().bind(dimensionService.gapSmallProperty());

        // Create 10 candy token slots
        for (int i = 0; i < 10; i++) {
            ImageView tokenSlot = createCandyTokenSlot(i);
            container.getChildren().add(tokenSlot);
        }

        return container;
    }

    private ImageView createCandyTokenSlot(int slotIndex) {
        ImageView tokenView = new ImageView();

        // Bind size to dimension service
        tokenView.fitWidthProperty().bind(dimensionService.candyTokenSizeProperty());
        tokenView.fitHeightProperty().bind(dimensionService.candyTokenSizeProperty());
        tokenView.setPreserveRatio(true);
        tokenView.setSmooth(true);

        // Set initial image based on slot index and active candy count
        updateCandyTokenSlot(tokenView, slotIndex);

        return tokenView;
    }

    private void updateCandyTokenSlot(ImageView tokenView, int slotIndex) {
        if (slotIndex < activeCandyCount) {
            tokenView.setImage(candyImage);
            tokenView.setOpacity(1.0);
        } else {
            tokenView.setImage(candyOutlineImage);
            tokenView.setOpacity(0.5);
        }
    }

    private HBox createCoffeeContainer() {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.spacingProperty().bind(dimensionService.gapSmallProperty());

        // Coffee icon
        ImageView coffeeIcon = new ImageView();
        if (coffeeImage != null) {
            coffeeIcon.setImage(coffeeImage);
        }
        coffeeIcon.fitWidthProperty().bind(dimensionService.coffeeIconSizeProperty());
        coffeeIcon.fitHeightProperty().bind(dimensionService.coffeeIconSizeProperty());
        coffeeIcon.setPreserveRatio(true);
        coffeeIcon.setSmooth(true);

        // Coffee count label - NO inline styles
        Label coffeeLabel = new Label("x" + coffeeCount);
        coffeeLabel.getStyleClass().add("coffee-label");

        container.getChildren().addAll(coffeeIcon, coffeeLabel);
        return container;
    }

    private Button createEndTurnButton() {
        Button button = new Button("End Turn");
        button.getStyleClass().add("end-turn-button");

        HBox.setHgrow(button, javafx.scene.layout.Priority.NEVER);
        return button;
    }

    // Callback for end turn button
    private Runnable endTurnCallback;

    /**
     * Set the callback to be executed when the end turn button is clicked.
     */
    public void setEndTurnCallback(Runnable callback) {
        this.endTurnCallback = callback;
        endTurnButton.setOnAction(event -> {
            if (endTurnCallback != null) {
                endTurnCallback.run();
            }
        });
    }

    /**
     * Update the display with new candy and coffee counts.
     */
    public void updateStash(int candyCount, int coffeeCount) {
        this.activeCandyCount = Math.max(0, Math.min(10, candyCount));
        this.coffeeCount = Math.max(0, coffeeCount);

        // Update candy token displays
        for (int i = 0; i < candyTokensContainer.getChildren().size(); i++) {
            ImageView tokenView = (ImageView) candyTokensContainer.getChildren().get(i);
            updateCandyTokenSlot(tokenView, i);
        }

        // Update coffee label
        if (!coffeeContainer.getChildren().isEmpty() && coffeeContainer.getChildren().size() > 1) {
            Label coffeeLabel = (Label) coffeeContainer.getChildren().get(1);
            coffeeLabel.setText("x" + this.coffeeCount);
        }
    }

    /**
     * Enable or disable the end turn button.
     */
    public void setEndTurnEnabled(boolean enabled) {
        endTurnButton.setDisable(!enabled);
    }

    // Getters
    public int getActiveCandyCount() {
        return activeCandyCount;
    }

    public int getCoffeeCount() {
        return coffeeCount;
    }

    public Button getEndTurnButton() {
        return endTurnButton;
    }
}
