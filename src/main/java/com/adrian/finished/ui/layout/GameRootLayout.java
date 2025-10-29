package com.adrian.finished.ui.layout;

import com.adrian.finished.ui.DimensionService;
import javafx.scene.layout.StackPane;

/**
 * Root layout container for the entire game UI.
 * Contains the game areas and overlays like the finished pile.
 */
public class GameRootLayout extends StackPane {

    private final DimensionService dimensionService;
    private final GameAreasLayout gameAreasLayout;
    private final FinishedPileOverlay finishedPileOverlay;

    public GameRootLayout(DimensionService dimensionService) {
        super();
        this.dimensionService = dimensionService;

        // Set CSS style class
        getStyleClass().add("game-root");

        // Create main game areas layout
        this.gameAreasLayout = new GameAreasLayout(dimensionService);

        // Create finished pile overlay
        this.finishedPileOverlay = new FinishedPileOverlay(dimensionService);

        // Add components to stack pane (game areas first, then overlay)
        getChildren().addAll(gameAreasLayout, finishedPileOverlay);

        // Bind sizes
        setupLayout();

        System.out.println("GameRootLayout created with " + getChildren().size() + " children");
    }

    private void setupLayout() {
        // The StackPane will automatically size children to fill the available space
        // Individual components will handle their own sizing through the DimensionService

        // Position the finished pile overlay in bottom-right corner
        StackPane.setAlignment(finishedPileOverlay, javafx.geometry.Pos.BOTTOM_RIGHT);

        // Add margins to the finished pile overlay
        StackPane.setMargin(finishedPileOverlay, new javafx.geometry.Insets(
            0, // top
            dimensionService.gapMediumProperty().get(), // right
            dimensionService.stashBarHeightProperty().get() + dimensionService.gapMediumProperty().get(), // bottom
            0 // left
        ));

        // Ensure overlay is positioned correctly
        finishedPileOverlay.toFront();
    }

    public GameAreasLayout getGameAreasLayout() {
        return gameAreasLayout;
    }

    public FinishedPileOverlay getFinishedPileOverlay() {
        return finishedPileOverlay;
    }
}
