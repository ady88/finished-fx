package com.adrian;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A minimal JavaFX application that can also run on JPro.
 * - Shows a centered "Hello, world" text with a FontAwesome icon to its right.
 * - No hardcoded sizes: uses Dimensions to compute values from screen/scene size.
 * - Fullscreen by default on desktop; windowed in browser (JPro).
 */
public class FinishedFxApp extends Application {

    private static boolean isRunningInBrowser() {
        try {
            Class<?> webApi = Class.forName("one.jpro.webapi.WebAPI");
            var method = webApi.getMethod("isBrowser");
            Object result = method.invoke(null);
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable ignore) {
            // Class not on classpath or any reflection error -> assume desktop
            return false;
        }
    }

    @Override
    public void start(Stage stage) {
        // Label + icon in a centered horizontal box
        Label hello = new Label("Hello, world");
        FontIcon icon = new FontIcon(FontAwesomeSolid.THUMBS_UP);

        HBox content = new HBox(hello, icon);
        content.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(content);
        root.setAlignment(Pos.CENTER);

        // Create scene sized to current screen bounds (no hardcoded constants)
        double screenW = Screen.getPrimary().getBounds().getWidth();
        double screenH = Screen.getPrimary().getBounds().getHeight();
        Scene scene = new Scene(root, screenW * 0.8, screenH * 0.8);

        // Dimensions helper observes scene size and updates UI accordingly
        Dimensions dims = new Dimensions(scene.widthProperty(), scene.heightProperty());
        hello.styleProperty().bind(dims.fontSizeStyleProperty());
        root.paddingProperty().bind(dims.paddingProperty());
        // Bind the icon size responsively and use it to derive spacing
        icon.iconSizeProperty().bind(dims.iconSizeProperty());
        content.spacingProperty().bind(dims.iconSizeProperty().multiply(0.3));

        stage.setTitle("Finished FX");
        stage.setScene(scene);

        // Detect if running on JPro (in browser) without a hard dependency (uses reflection)
        boolean runningInBrowser = isRunningInBrowser();

        if (!runningInBrowser) {
            // Desktop: go fullscreen by default
            stage.setFullScreenExitHint(""); // no hint text
            stage.setFullScreen(true);
        } else {
            // Browser: don't request fullscreen; size is managed by JPro
            // Ensure layout updates after initial render
            Platform.runLater(root::requestLayout);
        }

        stage.show();
    }

    // Helper to obtain an observable for spacing calculation before scene is created
    private static javafx.beans.value.ObservableNumberValue dimsIconSizeBinding(HBox hBox) {
        // Use the width property of the scene once available; fall back to a simple constant until then
        // but derive spacing from icon size binding after scene is set
        return Bindings.when(hBox.sceneProperty().isNull())
                .then(20)
                .otherwise(Bindings.createIntegerBinding(
                        () -> (int) Math.round(Math.min(
                                hBox.getScene().getWidth(),
                                hBox.getScene().getHeight()) * 0.08),
                        hBox.sceneProperty(),
                        hBox.sceneProperty().get().widthProperty(),
                        hBox.sceneProperty().get().heightProperty()
                ));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
