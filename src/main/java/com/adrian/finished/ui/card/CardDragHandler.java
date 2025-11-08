package com.adrian.finished.ui.card;

import com.adrian.finished.model.Card;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Handles drag and drop functionality for InteractiveCardComponent.
 * Supports both desktop JavaFX and JPro web environments with appropriate
 * visual feedback strategies.
 */
public class CardDragHandler {

    private final InteractiveCardComponent cardComponent;

    // Drag and drop state
    private boolean isDragInProgress = false;

    // JPro detection - shared static to avoid repeated detection
    private static Boolean isJProMode = null;

    public CardDragHandler(InteractiveCardComponent cardComponent) {
        this.cardComponent = cardComponent;
        setupDragAndDropHandlers();
    }

    /**
     * Detect if we're running in JPro mode with manual override capability
     */
    private static boolean isJProMode() {
        if (isJProMode == null) {
            try {
                // Check for manual override first
                String forceJPro = System.getProperty("force.jpro.mode");
                if ("true".equals(forceJPro)) {
                    isJProMode = true;
                    System.out.println("🔍 JPro mode FORCED via system property");
                    return isJProMode;
                }
                if ("false".equals(forceJPro)) {
                    isJProMode = false;
                    System.out.println("🔍 Desktop mode FORCED via system property");
                    return isJProMode;
                }

                // Try WebAPI.isBrowser() - this is the most reliable method
                try {
                    Class<?> webAPIClass = Class.forName("com.jpro.webapi.WebAPI");
                    java.lang.reflect.Method isBrowserMethod = webAPIClass.getMethod("isBrowser");
                    Object result = isBrowserMethod.invoke(null);
                    isJProMode = Boolean.TRUE.equals(result);
                    System.out.println("🔍 Environment detected via WebAPI.isBrowser(): " + (isJProMode ? "JPro" : "Desktop"));
                } catch (ClassNotFoundException e) {
                    // WebAPI not in classpath - definitely not JPro
                    isJProMode = false;
                    System.out.println("🔍 Desktop mode detected (WebAPI not in classpath)");
                } catch (Exception e) {
                    // WebAPI available but method call failed - probably desktop
                    isJProMode = false;
                    System.out.println("🔍 Desktop mode detected (WebAPI call failed: " + e.getMessage() + ")");
                }

            } catch (Exception e) {
                System.out.println("⚠️ Error detecting JPro mode, defaulting to desktop: " + e.getMessage());
                isJProMode = false;
            }
        }
        return isJProMode;
    }

    private void setupDragAndDropHandlers() {
        cardComponent.setOnDragDetected(this::handleDragDetected);
        cardComponent.setOnDragOver(this::handleDragOver);
        cardComponent.setOnDragEntered(this::handleDragEntered);
        cardComponent.setOnDragExited(this::handleDragExited);
        cardComponent.setOnDragDropped(this::handleDragDropped);
        cardComponent.setOnDragDone(this::handleDragDone);
    }

    private void handleDragDetected(MouseEvent event) {
        if (cardComponent.isDisabled()) {
            return;
        }

        // Start drag operation
        Dragboard dragboard = cardComponent.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();

        // Use a simple identifier for the drag operation
        content.putString("interactive-card-" + cardComponent.getCard().number());
        dragboard.setContent(content);

        // Choose strategy based on environment
        if (isJProMode()) {
            // Use BufferedImage approach for web/JPro (works reliably in browser)
            if (!setupBufferedImageDragView(dragboard, event)) {
                // Fallback to CSS-only feedback if BufferedImage fails
                setupJProDragFeedback();
            }
        } else {
            // Use original snapshot approach for desktop (optimal performance)
            setupDesktopDragView(dragboard, event);
        }

        isDragInProgress = true;
        event.consume();
    }

    /**
     * Create drag view using BufferedImage approach (should work in both desktop and JPro)
     */
    private boolean setupBufferedImageDragView(Dragboard dragboard, MouseEvent event) {
        try {
            System.out.println("🎯 Attempting BufferedImage drag view approach...");

            // Get dimensions from the card component
            int width = (int) Math.max(100, cardComponent.getWidth());
            int height = (int) Math.max(40, cardComponent.getHeight());

            // Create a BufferedImage with transparency support
            BufferedImage dummyBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = dummyBuffer.createGraphics();

            // Enable antialiasing for better quality
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Try to get the actual card image if available
            if (!cardComponent.getChildren().isEmpty() &&
                    cardComponent.getChildren().getFirst() instanceof ImageView imageView &&
                    imageView.getImage() != null && !imageView.getImage().isError()) {

                // Convert JavaFX Image to BufferedImage if possible
                try {
                    BufferedImage cardImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    if (cardImage != null) {
                        // Draw the card image scaled to fit
                        graphics.drawImage(cardImage, 0, 0, width, height, null);
                        System.out.println("✅ Used actual card image in drag view");
                    } else {
                        // Fallback to colored rectangle
                        drawFallbackDragImage(graphics, width, height);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Could not convert card image, using fallback: " + e.getMessage());
                    drawFallbackDragImage(graphics, width, height);
                }
            } else {
                // No card image available, draw fallback
                drawFallbackDragImage(graphics, width, height);
            }

            graphics.dispose();

            // Convert BufferedImage to JavaFX Image
            Image fxImage = SwingFXUtils.toFXImage(dummyBuffer, null);

            if (!fxImage.isError()) {
                // Calculate offset based on mouse position
                double offsetX = Math.max(0, Math.min(event.getX(), width));
                double offsetY = Math.max(0, Math.min(event.getY(), height));

                dragboard.setDragView(fxImage, offsetX, offsetY);
                System.out.println("✅ BufferedImage drag view created successfully: " + width + "x" + height);

                // Add visual feedback to source
                cardComponent.getStyleClass().add("drag-source");
                cardComponent.setOpacity(0.7);

                return true;
            }

        } catch (Exception e) {
            System.err.println("❌ BufferedImage drag view creation failed: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Draw a fallback drag image when the actual card image is not available
     */
    private void drawFallbackDragImage(Graphics2D graphics, int width, int height) {
        // Create a card-like appearance
        // Background
        graphics.setColor(new java.awt.Color(240, 240, 240, 200)); // Light gray with transparency
        graphics.fillRoundRect(0, 0, width, height, 10, 10);

        // Border
        graphics.setColor(new java.awt.Color(100, 100, 100, 180));
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRoundRect(1, 1, width - 2, height - 2, 10, 10);

        // Card number or identifier
        Card card = cardComponent.getCard();
        if (card != null) {
            graphics.setColor(new java.awt.Color(50, 50, 50, 200));
            graphics.setFont(new Font("Arial", Font.BOLD, Math.min(width / 6, height / 3)));
            FontMetrics fm = graphics.getFontMetrics();
            String text = String.valueOf(card.number());
            int textX = (width - fm.stringWidth(text)) / 2;
            int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
            graphics.drawString(text, textX, textY);
        }

        System.out.println("📝 Drew fallback drag image");
    }

    /**
     * Set up drag view for desktop JavaFX (original approach as fallback)
     */
    private void setupDesktopDragView(Dragboard dragboard, MouseEvent event) {
        try {
            // Get the ImageView from the parent CardComponent
            System.out.println("ADRIAN 1");
            if (!cardComponent.getChildren().isEmpty() &&
                    cardComponent.getChildren().getFirst() instanceof ImageView imageView) {

                System.out.println("ADRIAN 2");
                // Validate image before snapshot
                if (imageView.getImage() != null && !imageView.getImage().isError()) {
                    System.out.println("ADRIAN 3");
                    // Use robust snapshot approach
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    System.out.println("ADRIAN 4");
                    params.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    System.out.println("ADRIAN 5");
                    javafx.scene.image.WritableImage dragImage = imageView.snapshot(params, null);
                    System.out.println("ADRIAN 6");
                    if (dragImage != null && !dragImage.isError()) {
                        System.out.println("ADRIAN 7");
                        // Calculate proper offset
                        double imageX = (cardComponent.getWidth() - imageView.getFitWidth()) / 2;
                        double imageY = (cardComponent.getHeight() - imageView.getFitHeight()) / 2;
                        double offsetX = Math.max(0, Math.min(event.getX() - imageX, dragImage.getWidth()));
                        double offsetY = Math.max(0, Math.min(event.getY() - imageY, dragImage.getHeight()));
                        System.out.printf("ADRIAN 8: offsetX=%.2f offsetY=%.2f%n", offsetX, offsetY);
                        dragboard.setDragView(dragImage, offsetX, offsetY);
                        System.out.println("ADRIAN 9");
                        System.out.println("✅ Desktop drag view created successfully: " +
                                dragImage.getWidth() + "x" + dragImage.getHeight());
                    }
                }
            }

            // Add visual feedback to source
            cardComponent.getStyleClass().add("drag-source");
            cardComponent.setOpacity(0.7);

        } catch (Exception e) {
            System.err.println("❌ Desktop drag view creation failed: " + e.getMessage());
            // Fallback to JPro-style feedback
            setupJProDragFeedback();
        }
    }

    /**
     * Set up JPro-compatible drag feedback (CSS only, no setDragView)
     */
    private void setupJProDragFeedback() {
        System.out.println("🌐 Using JPro-compatible drag feedback for card " + cardComponent.getCard().number());

        // Add visual feedback classes - make them more prominent for JPro
        cardComponent.getStyleClass().addAll("drag-source", "jpro-dragging");

        // Make the visual feedback more obvious since there's no floating image
        cardComponent.setOpacity(0.3); // More transparent
        cardComponent.setScaleX(0.8);  // Smaller
        cardComponent.setScaleY(0.8);
    }

    private void handleDragOver(DragEvent event) {
        // Accept the drag if it's from another InteractiveCardComponent
        if (event.getGestureSource() != cardComponent &&
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
        if (event.getGestureSource() != cardComponent &&
                event.getGestureSource() instanceof InteractiveCardComponent) {
            cardComponent.getStyleClass().add("drag-target");

            // Make drop target more obvious in JPro mode
            if (isJProMode()) {
                cardComponent.setScaleX(1.1);
                cardComponent.setScaleY(1.1);
            }
        }
        event.consume();
    }

    private void handleDragExited(DragEvent event) {
        // Remove visual feedback
        cardComponent.getStyleClass().remove("drag-target");

        // Restore scale in JPro mode
        if (isJProMode() && !cardComponent.getStyleClass().contains("drag-source")) {
            cardComponent.setScaleX(1.0);
            cardComponent.setScaleY(1.0);
        }

        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        boolean success = false;

        if (event.getGestureSource() instanceof InteractiveCardComponent sourceCard &&
                sourceCard != cardComponent) {

            // Delegate to the card's swap callback
            InteractiveCardComponent.CardSwapCallback swapCallback = cardComponent.getCardSwapCallback();
            if (swapCallback != null) {
                swapCallback.onCardSwap(sourceCard, cardComponent);
                success = true;
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    private void handleDragDone(DragEvent event) {
        // Clean up drag state
        cleanupDragOperation();

        System.out.println("Drag operation completed: " + event.getTransferMode());
        event.consume();
    }

    /**
     * Clean up all drag visual effects
     */
    private void cleanupDragOperation() {
        cardComponent.getStyleClass().removeAll("drag-source", "drag-target", "jpro-dragging");
        cardComponent.setOpacity(1.0);
        cardComponent.setScaleX(1.0);
        cardComponent.setScaleY(1.0);
        isDragInProgress = false;
    }

    public boolean isDragInProgress() {
        return isDragInProgress;
    }
}