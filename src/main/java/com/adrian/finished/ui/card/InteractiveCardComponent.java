package com.adrian.finished.ui.card;

import com.adrian.finished.model.Card;
import com.adrian.finished.ui.DimensionService;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.image.ImageView;

/**
 * Interactive version of CardComponent that supports click interactions,
 * candy activation, and drag-and-drop reordering for cards in the Present area.
 */
public class InteractiveCardComponent extends CardComponent {

    private Runnable onCandyActivation;
    private Runnable onClick;
    private CardSwapCallback onCardSwap;

    // Drag and drop state
    private boolean isDragInProgress = false;
    private double originalLayoutX;
    private double originalLayoutY;

    @FunctionalInterface
    public interface CardSwapCallback {
        void onCardSwap(InteractiveCardComponent sourceCard, InteractiveCardComponent targetCard);
    }

    public InteractiveCardComponent(DimensionService dimensionService, Card card) {
        super(dimensionService, card, false); // Interactive cards are always normal size

        // Ensure no background or border styling that could interfere with drag view
        setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;");

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

        // Set up drag and drop
        setupDragAndDrop();

        // Make sure we're not disabled by default (unlike base CardComponent)
        setDisabled(false);
    }

    private void setupDragAndDrop() {
        // Set up drag detection
        setOnDragDetected(this::handleDragDetected);

        // Set up drag over (when another card is dragged over this one)
        setOnDragOver(this::handleDragOver);

        // Set up drag entered (visual feedback)
        setOnDragEntered(this::handleDragEntered);

        // Set up drag exited (remove visual feedback)
        setOnDragExited(this::handleDragExited);

        // Set up drag dropped (complete the swap)
        setOnDragDropped(this::handleDragDropped);

        // Set up drag done (cleanup)
        setOnDragDone(this::handleDragDone);
    }

    private void handleDragDetected(MouseEvent event) {
        if (isDisabled()) {
            return;
        }

        // Store original position for potential snap-back
        originalLayoutX = getLayoutX();
        originalLayoutY = getLayoutY();

        // Start drag operation
        Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();

        // Use a simple identifier for the drag operation
        content.putString("interactive-card-" + getCard().number());
        dragboard.setContent(content);

        // Create a snapshot of just the card image (not the container)
        try {
            // Get the ImageView from the parent CardComponent
            ImageView imageView = (ImageView) getChildren().get(0);

            // Take snapshot of just the ImageView to avoid container background
            javafx.scene.image.WritableImage dragImage = imageView.snapshot(null, null);

            // Calculate the offset based on where the user clicked relative to the image
            double imageX = (getWidth() - imageView.getFitWidth()) / 2;
            double imageY = (getHeight() - imageView.getFitHeight()) / 2;
            double offsetX = event.getX() - imageX;
            double offsetY = event.getY() - imageY;

            dragboard.setDragView(dragImage, offsetX, offsetY);
        } catch (Exception e) {
            System.err.println("Failed to create drag view: " + e.getMessage());
            // Fallback: continue with default drag view
        }

        // Hide the source card during dragging
        setVisible(false);
        isDragInProgress = true;

        event.consume();
    }

    private void handleDragOver(DragEvent event) {
        // Accept the drag if it's from another InteractiveCardComponent
        if (event.getGestureSource() != this &&
            event.getGestureSource() instanceof InteractiveCardComponent &&
            event.getDragboard().hasString()) {

            String content = event.getDragboard().getString();
            if (content.startsWith("interactive-card-")) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        }
        event.consume();
    }

    private void handleDragEntered(DragEvent event) {
        // Add visual feedback when a card is dragged over this one
        if (event.getGestureSource() != this &&
            event.getGestureSource() instanceof InteractiveCardComponent) {
            getStyleClass().add("drag-target");
        }
        event.consume();
    }

    private void handleDragExited(DragEvent event) {
        // Remove visual feedback
        getStyleClass().remove("drag-target");
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        boolean success = false;

        if (event.getGestureSource() instanceof InteractiveCardComponent sourceCard &&
            sourceCard != this) {

            // Perform the card swap
            if (onCardSwap != null) {
                onCardSwap.onCardSwap(sourceCard, this);
                success = true;
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    private void handleDragDone(DragEvent event) {
        // Clean up drag state and make card visible again
        getStyleClass().remove("drag-source");
        getStyleClass().remove("drag-target");
        setVisible(true); // Always restore visibility
        isDragInProgress = false;

        // If drag was not successful, card will be back in its original position
        // due to the layout not changing

        event.consume();
    }

    private void handleMouseClick(MouseEvent event) {
        // Don't handle clicks if a drag is in progress
        if (isDragInProgress) {
            return;
        }

        if (isDisabled()) {
            return;
        }

        Card card = getCard();
        if (card == null) {
            return;
        }

        // Simple click activation for candy (no modifier keys needed)
        if (event.isPrimaryButtonDown()) {
            handleCandyActivation();
        }
    }

    private void handleCandyActivation() {
        if (!isCandyActivated()) {
            // Try to activate with candy
            if (tryActivateWithCandy()) {
                // Notify callback
                if (onCandyActivation != null) {
                    onCandyActivation.run();
                }
            }
        }
    }

    private void handleRegularClick() {
        if (onClick != null) {
            onClick.run();
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

    // Override to allow external control of candy activation state
    @Override
    public void setCandyActivated(boolean candyActivated) {
        super.setCandyActivated(candyActivated);

        // Update interactive state
        if (candyActivated) {
            getStyleClass().add("candy-activated");
        } else {
            getStyleClass().remove("candy-activated");
        }
    }
}
