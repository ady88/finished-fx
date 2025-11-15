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

    protected final DimensionService dimensionService;
    private final ObjectProperty<Card> card = new SimpleObjectProperty<>();
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
        // Update image when card changes - we only need to listen to card changes
        // since the Card model contains abilitiesTriggered which drives visual state
        card.addListener((obs, oldCard, newCard) -> updateCardImage());

        // Initial update
        updateCardImage();
    }


    private void updateCardImage() {
        Card currentCard = card.get();
        if (currentCard == null) {
            backgroundImage.setImage(null);
            return;
        }

        // For cards with multiple candy slots (maxAbilities > 1), use Card model state
        // For cards with single candy slot (maxAbilities <= 1), use either Card model or UI state
        boolean shouldShowCandy;
        if (hasMultipleCandySlots(currentCard)) {
            // Multi-candy cards: always use Card model state
            shouldShowCandy = currentCard.abilitiesTriggered() > 0;
        } else {
            // Single-candy cards: use Card model state OR UI state (for compatibility)
            shouldShowCandy = currentCard.abilitiesTriggered() > 0; // || candyActivated.get();
        }

        String imagePath = buildImagePath(currentCard.number(), shouldShowCandy, smallVariant.get());
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                // Fallback to normal variant if candy version doesn't exist
                if (shouldShowCandy) {
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
        String suffix;

        if (small) {
            // Small variant
            if (candyState) {
                Card currentCard = card.get();
                if (currentCard != null && hasMultipleCandySlots(currentCard)) {
                    // Multi-candy cards: use specific candy level
                    int candyLevel = Math.max(1, currentCard.abilitiesTriggered()); // At least 1 if showing candy
                    suffix = "_cardc_" + candyLevel + "_small.png";
                } else {
                    // Single-candy cards: use standard candy variant
                    suffix = "_cardc_small.png";
                }
            } else {
                suffix = "_card_small.png";
            }
        } else {
            // Normal variant
            if (candyState) {
                Card currentCard = card.get();
                if (currentCard != null && hasMultipleCandySlots(currentCard)) {
                    // Multi-candy cards: use specific candy level
                    int candyLevel = Math.max(1, currentCard.abilitiesTriggered()); // At least 1 if showing candy
                    suffix = "_cardc_" + candyLevel + ".png";
                } else {
                    // Single-candy cards: use standard candy variant
                    suffix = "_cardc.png";
                }
            } else {
                suffix = "_card.png";
            }
        }

        return basePath + cardNumber + suffix;
    }

    /**
     * Attempts to activate the card with candy if eligible.
     * Uses Card model logic for cards with multiple candy slots.
     * Uses UI boolean logic for cards with single candy slot (backward compatibility).
     * @return true if candy was successfully applied, false otherwise
     */
    public boolean tryActivateWithCandy() {
        Card currentCard = card.get();
        if (currentCard == null) {
            return false; // No card to activate
        }

        // For multi-candy cards, use Card model logic
        if (hasMultipleCandySlots(currentCard)) {
            // Check if the card can still trigger abilities (based on Card model logic)
            if (!currentCard.canTriggerAbility()) {
                return false; // Card has reached its ability limit
            }

            // Check if the next candy level image exists
            if (!isCandyEligible()) {
                return false; // Not eligible for candy (image doesn't exist)
            }

            // For multi-candy cards, don't modify UI state - let the Card model drive everything
            return true;
        } else {

            if (!isCandyEligible()) {
                return false; // Not eligible for candy
            }

            return true;
        }
    }

    /**
     * UI-only heuristic for determining candy eligibility.
     * For multi-candy cards, check if the next candy level image exists.
     * For single-candy cards, check if the candy variant image exists.
     */
    private boolean isCandyEligible() {
        Card currentCard = card.get();
        if (currentCard == null) return false;

        if (hasMultipleCandySlots(currentCard)) {
            // Multi-candy cards: check if the next candy level image exists
            int nextCandyLevel = currentCard.abilitiesTriggered() + 1;
            if (nextCandyLevel > currentCard.maxAbilities()) {
                return false; // Already at maximum candy level
            }

            // Build path for the next candy level
            String basePath = "/assets/cards/";
            String suffix = smallVariant.get()
                    ? "_cardc_" + nextCandyLevel + "_small.png"
                    : "_cardc_" + nextCandyLevel + ".png";
            String candyImagePath = basePath + currentCard.number() + suffix;

            try {
                Image testImage = new Image(getClass().getResourceAsStream(candyImagePath));
                return !testImage.isError();
            } catch (Exception e) {
                return false;
            }
        } else {
            // Single-candy cards: check if the standard candy variant exists
            String candyImagePath = buildImagePath(currentCard.number(), true, smallVariant.get());
            try {
                Image testImage = new Image(getClass().getResourceAsStream(candyImagePath));
                return !testImage.isError();
            } catch (Exception e) {
                return false;
            }
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

    public BooleanProperty smallVariantProperty() { return smallVariant; }
    public boolean isSmallVariant() { return smallVariant.get(); }


    /**
     * Determines if this card can still be activated with candy.
     * For multi-candy cards, uses Card model logic.
     * For single-candy cards, uses UI boolean logic.
     */
    public boolean canBeActivatedWithCandy() {
        Card currentCard = card.get();
        if (currentCard == null) return false;

        if (hasMultipleCandySlots(currentCard)) {
            // Multi-candy cards: use Card model logic
            return currentCard.canTriggerAbility() && isCandyEligible();
        } else {
            // Single-candy cards: use UI boolean logic
            return isCandyEligible(); //!candyActivated.get() &&
        }
    }

    /**
     * Determines if this card supports multiple candy slots.
     */
    private boolean hasMultipleCandySlots(Card card) {
        return card.maxAbilities() > 1;
    }

}
