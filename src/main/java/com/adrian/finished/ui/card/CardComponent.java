package com.adrian.finished.ui.card;

import com.adrian.finished.model.Card;
import com.adrian.finished.ui.DimensionService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Visual representation of a Card that supports normal and candy-activated states.
 * Supports two size variants: normal (for Present area) and small (for Future, Past, Finished pile).
 */
public class CardComponent extends StackPane {

    private final DimensionService dimensionService;
    private final ObjectProperty<Card> card = new SimpleObjectProperty<>();
    private final BooleanProperty candyActivated = new SimpleBooleanProperty(false);
    private final BooleanProperty smallVariant = new SimpleBooleanProperty(false);

    private final ImageView backgroundImage;

    public CardComponent(DimensionService dimensionService, Card card, boolean smallVariant) {
        this.dimensionService = dimensionService;
        this.smallVariant.set(smallVariant);

        // If small variant, disable interaction using Node's setDisable
        setDisable(smallVariant);

        // Set up the background image
        this.backgroundImage = new ImageView();
        this.backgroundImage.setPreserveRatio(true);
        this.backgroundImage.setSmooth(true);

        getChildren().add(backgroundImage);
        getStyleClass().add("card-component");

        // Bind dimensions based on variant
        if (smallVariant) {
            bindSmallCardDimensions();
        } else {
            bindNormalCardDimensions();
        }

        // Set up property listeners
        setupPropertyListeners();

        // Set the initial card
        this.card.set(card);
    }

    private void bindNormalCardDimensions() {
        prefWidthProperty().bind(dimensionService.cardWidthProperty());
        prefHeightProperty().bind(dimensionService.cardHeightProperty());
        minWidthProperty().bind(dimensionService.cardWidthProperty());
        minHeightProperty().bind(dimensionService.cardHeightProperty());
        maxWidthProperty().bind(dimensionService.cardWidthProperty());
        maxHeightProperty().bind(dimensionService.cardHeightProperty());

        backgroundImage.fitWidthProperty().bind(dimensionService.cardWidthProperty());
        backgroundImage.fitHeightProperty().bind(dimensionService.cardHeightProperty());
    }

    private void bindSmallCardDimensions() {
        prefWidthProperty().bind(dimensionService.smallCardWidthProperty());
        prefHeightProperty().bind(dimensionService.smallCardHeightProperty());
        minWidthProperty().bind(dimensionService.smallCardWidthProperty());
        minHeightProperty().bind(dimensionService.smallCardHeightProperty());
        maxWidthProperty().bind(dimensionService.smallCardWidthProperty());
        maxHeightProperty().bind(dimensionService.smallCardHeightProperty());

        backgroundImage.fitWidthProperty().bind(dimensionService.smallCardWidthProperty());
        backgroundImage.fitHeightProperty().bind(dimensionService.smallCardHeightProperty());
    }

    private void setupPropertyListeners() {
        // Update image when card or candy state changes
        card.addListener((obs, oldCard, newCard) -> updateCardImage());
        candyActivated.addListener((obs, oldState, newState) -> updateCardImage());

        // Initial update
        updateCardImage();
    }

    private void updateCardImage() {
        Card currentCard = card.get();
        if (currentCard == null) {
            backgroundImage.setImage(null);
            return;
        }

        String imagePath = buildImagePath(currentCard.number(), candyActivated.get(), smallVariant.get());
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                // Fallback to normal variant if candy version doesn't exist
                if (candyActivated.get()) {
                    String fallbackPath = buildImagePath(currentCard.number(), false, smallVariant.get());
                    image = new Image(getClass().getResourceAsStream(fallbackPath));
                }
            }
            backgroundImage.setImage(image);
        } catch (Exception e) {
            System.err.println("Failed to load card image: " + imagePath);
            backgroundImage.setImage(null);
        }
    }

    private String buildImagePath(int cardNumber, boolean candyState, boolean small) {
        String basePath = "/assets/cards/";
        String suffix = small ? "_card_small.png" : (candyState ? "_cardc.png" : "_card.png");
        return basePath + cardNumber + suffix;
    }

    /**
     * Attempts to activate the card with candy if eligible.
     * @return true if candy was successfully applied, false otherwise
     */
    public boolean tryActivateWithCandy() {
        if (candyActivated.get()) {
            return false; // Already activated
        }

        if (!isCandyEligible()) {
            return false; // Not eligible for candy
        }

        candyActivated.set(true);
        return true;
    }

    /**
     * UI-only heuristic for determining candy eligibility.
     * A card is eligible if its candy variant image exists.
     */
    private boolean isCandyEligible() {
        if (card.get() == null) return false;

        String candyImagePath = buildImagePath(card.get().number(), true, smallVariant.get());
        try {
            Image testImage = new Image(getClass().getResourceAsStream(candyImagePath));
            return !testImage.isError();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Override the default size bindings with custom dimensions.
     * Useful for special cases like finished pile overlay where cards need to be smaller.
     */
    public void overrideDimensions(javafx.beans.binding.DoubleBinding customWidth, javafx.beans.binding.DoubleBinding customHeight) {
        // Unbind from dimension service
        prefWidthProperty().unbind();
        prefHeightProperty().unbind();
        minWidthProperty().unbind();
        minHeightProperty().unbind();
        maxWidthProperty().unbind();
        maxHeightProperty().unbind();

        backgroundImage.fitWidthProperty().unbind();
        backgroundImage.fitHeightProperty().unbind();

        // Bind to custom dimensions
        prefWidthProperty().bind(customWidth);
        prefHeightProperty().bind(customHeight);
        minWidthProperty().bind(customWidth);
        minHeightProperty().bind(customHeight);
        maxWidthProperty().bind(customWidth);
        maxHeightProperty().bind(customHeight);

        backgroundImage.fitWidthProperty().bind(customWidth);
        backgroundImage.fitHeightProperty().bind(customHeight);
    }

    // Property getters
    public ObjectProperty<Card> cardProperty() { return card; }
    public Card getCard() { return card.get(); }
    public void setCard(Card card) { this.card.set(card); }

    public BooleanProperty candyActivatedProperty() { return candyActivated; }
    public boolean isCandyActivated() { return candyActivated.get(); }
    public void setCandyActivated(boolean activated) { this.candyActivated.set(activated); }

    public BooleanProperty smallVariantProperty() { return smallVariant; }
    public boolean isSmallVariant() { return smallVariant.get(); }
}
