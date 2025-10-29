package com.adrian.finished.ui.layout;

import com.adrian.finished.ui.DimensionService;
import javafx.scene.layout.VBox;

/**
 * Main layout container for all game areas. Arranges areas vertically:
 * Future Areas, Present Area, Past Area, and Active Stash.
 */
public class GameAreasLayout extends VBox {

    private final DimensionService dimensionService;

    // Area layouts
    private final FutureAreasLayout futureAreasLayout;
    private final PresentAreaLayout presentAreaLayout;
    private final PastAreaLayout pastAreaLayout;
    private final ActiveStashLayout activeStashLayout;

    public GameAreasLayout(DimensionService dimensionService) {
        super();
        this.dimensionService = dimensionService;

        // Initialize all area layouts
        this.futureAreasLayout = new FutureAreasLayout(dimensionService);
        this.presentAreaLayout = new PresentAreaLayout(dimensionService);
        this.pastAreaLayout = new PastAreaLayout(dimensionService);
        this.activeStashLayout = new ActiveStashLayout(dimensionService);

        // Set up layout properties
        setupLayout();

        // Add all areas to this container
        getChildren().addAll(
            futureAreasLayout,
            presentAreaLayout,
            pastAreaLayout,
            activeStashLayout
        );

        System.out.println("GameAreasLayout initialized with " + getChildren().size() + " children:");
        System.out.println("- Future areas layout");
        System.out.println("- Present area layout");
        System.out.println("- Past area layout");
        System.out.println("- Active stash layout");
    }

    private void setupLayout() {
        // Bind heights to dimension service
        futureAreasLayout.prefHeightProperty().bind(dimensionService.futureAreaHeightProperty());
        presentAreaLayout.prefHeightProperty().bind(dimensionService.presentAreaHeightProperty());
        pastAreaLayout.prefHeightProperty().bind(dimensionService.pastAreaHeightProperty());
        activeStashLayout.prefHeightProperty().bind(dimensionService.activeStashHeightProperty());

        // Set spacing between areas
        spacingProperty().bind(dimensionService.gapSmallProperty());

        // Ensure areas expand to fill their allocated space
        setFillWidth(true);

        // Add debug listeners
        dimensionService.pastAreaHeightProperty().addListener((obs, oldVal, newVal) ->
            System.out.println("Past area height from DimensionService: " + newVal));
    }

    // Getter methods for area access
    public FutureAreasLayout getFutureAreasLayout() { return futureAreasLayout; }
    public PresentAreaLayout getPresentAreaLayout() { return presentAreaLayout; }
    public PastAreaLayout getPastAreaLayout() { return pastAreaLayout; }
    public ActiveStashLayout getActiveStashLayout() { return activeStashLayout; }
}
