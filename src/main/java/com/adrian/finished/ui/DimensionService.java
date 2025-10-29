package com.adrian.finished.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;

/**
 * Central, observable service for computing responsive dimensions based on scene size.
 * All UI components should use this service for sizing to ensure no hardcoded values.
 * Updates automatically when scene dimensions change.
 */
public class DimensionService {

    private final ReadOnlyDoubleProperty sceneWidth;
    private final ReadOnlyDoubleProperty sceneHeight;

    // Area heights (percentages of scene height)
    private final DoubleBinding futureAreaHeight;
    private final DoubleBinding presentAreaHeight;
    private final DoubleBinding pastAreaHeight;
    private final DoubleBinding stashBarHeight;

    // Card dimensions
    private final DoubleBinding cardHeight;
    private final DoubleBinding cardWidth;
    private final DoubleBinding smallCardHeight;
    private final DoubleBinding smallCardWidth;

    // Token sizes
    private final DoubleBinding candyTokenSize;
    private final DoubleBinding coffeeIconSize;

    // Typography
    private final DoubleBinding baseFontSize;
    private final DoubleBinding smallFontSize;

    // Spacing/gaps
    private final DoubleBinding gapSmall;
    private final DoubleBinding gapMedium;
    private final DoubleBinding gapLarge;

    // Overlay dimensions and positions
    private final DoubleBinding finishedPileWidth;
    private final DoubleBinding finishedPileHeight;
    private final DoubleBinding finishedPileBottom;
    private final DoubleBinding finishedPileRight;

    public DimensionService(ReadOnlyDoubleProperty sceneWidth, ReadOnlyDoubleProperty sceneHeight) {
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;

        // Area heights (30%, 40%, 24%, 6% of scene height)
        this.futureAreaHeight = sceneHeight.multiply(0.30);
        this.presentAreaHeight = sceneHeight.multiply(0.40);
        this.pastAreaHeight = sceneHeight.multiply(0.24);
        this.stashBarHeight = sceneHeight.multiply(0.06);

        // Card dimensions - base height is proportional to present area, width maintains aspect ratio
        this.cardHeight = presentAreaHeight.multiply(0.8); // Leave some padding in present area
        this.cardWidth = cardHeight.multiply(0.7); // Standard card aspect ratio

        // Small card dimensions (60% of normal cards)
        this.smallCardHeight = cardHeight.multiply(0.6);
        this.smallCardWidth = cardWidth.multiply(0.6);

        // Token sizes
        this.candyTokenSize = stashBarHeight.multiply(0.7);
        this.coffeeIconSize = stashBarHeight.multiply(0.6);

        // Typography - based on minimum dimension
        NumberBinding minDimension = Bindings.min(sceneWidth, sceneHeight);
        this.baseFontSize = Bindings.createDoubleBinding(
            () -> minDimension.doubleValue() * 0.02,
            minDimension
        );
        this.smallFontSize = baseFontSize.multiply(0.8);

        // Spacing - relative to scene size
        this.gapSmall = sceneWidth.multiply(0.005);
        this.gapMedium = sceneWidth.multiply(0.01);
        this.gapLarge = sceneWidth.multiply(0.02);

        // Overlay dimensions (10% width, 20% height)
        this.finishedPileWidth = sceneWidth.multiply(0.10);
        this.finishedPileHeight = sceneHeight.multiply(0.20);
        this.finishedPileBottom = stashBarHeight.add(gapMedium);
        this.finishedPileRight = gapMedium; // Distance from right edge
    }

    // Getters for properties
    public ReadOnlyDoubleProperty sceneWidthProperty() { return sceneWidth; }
    public ReadOnlyDoubleProperty sceneHeightProperty() { return sceneHeight; }

    public DoubleBinding futureAreaHeightProperty() { return futureAreaHeight; }
    public DoubleBinding presentAreaHeightProperty() { return presentAreaHeight; }
    public DoubleBinding pastAreaHeightProperty() { return pastAreaHeight; }
    public DoubleBinding stashBarHeightProperty() { return stashBarHeight; }

    public DoubleBinding cardHeightProperty() { return cardHeight; }
    public DoubleBinding cardWidthProperty() { return cardWidth; }
    public DoubleBinding smallCardHeightProperty() { return smallCardHeight; }
    public DoubleBinding smallCardWidthProperty() { return smallCardWidth; }

    // Active Stash height is the same as stash bar height
    public DoubleBinding activeStashHeightProperty() { return stashBarHeight; }

    public DoubleBinding candyTokenSizeProperty() { return candyTokenSize; }
    public DoubleBinding coffeeIconSizeProperty() { return coffeeIconSize; }

    public DoubleBinding baseFontSizeProperty() { return baseFontSize; }
    public DoubleBinding smallFontSizeProperty() { return smallFontSize; }

    public DoubleBinding gapSmallProperty() { return gapSmall; }
    public DoubleBinding gapMediumProperty() { return gapMedium; }
    public DoubleBinding gapLargeProperty() { return gapLarge; }

    public DoubleBinding finishedPileWidthProperty() { return finishedPileWidth; }
    public DoubleBinding finishedPileHeightProperty() { return finishedPileHeight; }
    public DoubleBinding finishedPileBottomProperty() { return finishedPileBottom; }
    public DoubleBinding finishedPileRightProperty() { return finishedPileRight; }
}
