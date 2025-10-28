package com.adrian;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Insets;

/**
 * Computes responsive dimensions based on the current scene size.
 * No hardcoded constants; everything is derived from width/height.
 */
public class Dimensions {
    private final ObservableNumberValue width;
    private final ObservableNumberValue height;

    private final StringBinding fontSizeStyle;
    private final ObjectBinding<Insets> padding;
    private final IntegerBinding iconSize;

    public Dimensions(ObservableNumberValue width, ObservableNumberValue height) {
        this.width = width;
        this.height = height;

        // Base unit is min(width, height)
        var base = Bindings.createDoubleBinding(
                () -> Math.min(this.width.doubleValue(), this.height.doubleValue()),
                this.width, this.height
        );

        // Font size ~ 7% of min dimension (tweakable factor)
        var fontSize = base.multiply(0.07);
        this.fontSizeStyle = Bindings.createStringBinding(
                () -> "-fx-font-size: " + String.format("%.2f", fontSize.get()) + "px;",
                fontSize
        );

        // Padding ~ 5% of min dimension
        var pad = base.multiply(0.05);
        this.padding = Bindings.createObjectBinding(
                () -> new Insets(pad.get(), pad.get(), pad.get(), pad.get()),
                pad
        );

        // Icon size ~ 8% of min dimension (rounded to nearest int)
        this.iconSize = Bindings.createIntegerBinding(
                () -> (int) Math.round(base.get() * 0.08),
                base
        );
    }

    public StringBinding fontSizeStyleProperty() {
        return fontSizeStyle;
    }

    public ObjectBinding<Insets> paddingProperty() {
        return padding;
    }

    public javafx.beans.binding.IntegerBinding iconSizeProperty() {
        return iconSize;
    }
}
