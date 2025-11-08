package com.adrian.finished.ui.controller;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.GameState;
import com.adrian.finished.model.abilities.AbilitySpec;
import com.adrian.finished.ui.DimensionService;
import com.adrian.finished.ui.card.InteractiveCardComponent;
import com.adrian.finished.ui.layout.*;
import com.adrian.finished.ui.pipeline.UIDecisionProvider;
import com.adrian.finished.ui.pipeline.GameLoopManager;
import com.adrian.finished.ui.pipeline.UIGameStateSynchronizer;
import com.adrian.finished.ui.pipeline.AbilityActivationManager;
import java.util.List;
import java.util.HashSet;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main game controller that manages the UI state and coordinates interactions
 * between different game areas. Handles mock data setup for Milestone 2.
 */
public class GameController {

    private final DimensionService dimensionService;
    private final GameRootLayout rootLayout;
    private final GameAreasLayout gameAreasLayout;
    private final UIDecisionProvider decisionProvider;
    private final GameLoopManager gameLoopManager;
    private final UIGameStateSynchronizer uiSynchronizer;
    private final AbilityActivationManager abilityActivationManager;

    // Area references
    private final PresentAreaLayout presentArea;
    private final FutureAreasLayout futureAreas;
    private final PastAreaLayout pastArea;
    private final ActiveStashLayout activeStash;
    private final FinishedPileOverlay finishedPile;

    public GameController(Stage primaryStage) {
        // Create scene first to get dimensions
        Scene scene = new Scene(new javafx.scene.layout.Pane(), 1200, 800);

        // Initialize dimension service
        this.dimensionService = new DimensionService(
            scene.widthProperty(),
            scene.heightProperty()
        );

        // Create root layout
        this.rootLayout = new GameRootLayout(dimensionService);
        this.gameAreasLayout = rootLayout.getGameAreasLayout();

        // Initialize decision provider
        this.decisionProvider = new UIDecisionProvider(rootLayout, dimensionService);

        // Initialize game loop system
        this.gameLoopManager = new GameLoopManager(decisionProvider);
        this.abilityActivationManager = new AbilityActivationManager();

        // Get references to all areas
        this.presentArea = gameAreasLayout.getPresentAreaLayout();
        this.futureAreas = gameAreasLayout.getFutureAreasLayout();
        this.pastArea = gameAreasLayout.getPastAreaLayout();
        this.activeStash = gameAreasLayout.getActiveStashLayout();
        this.finishedPile = rootLayout.getFinishedPileOverlay();

        // Initialize UI synchronizer
        this.uiSynchronizer = new UIGameStateSynchronizer(gameAreasLayout, finishedPile);

        // Set up interactions
        setupInteractions();

        // Set up game loop integration
        setupGameLoop();

        // Set up scene
        scene.setRoot(rootLayout);
        scene.getStylesheets().add(getClass().getResource("/styles/game.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Finished FX - Card Game UI");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
    }

    private void setupInteractions() {
        // Set up card activation callback for Present area
        presentArea.setCandyActivationCallback(this::handleCardActivation);

        // Set up card swap callback for Present area drag-and-drop
        presentArea.setCardSwapCallback(this::handleCardSwap);

        // Set up end turn button callback
        activeStash.setEndTurnCallback(this::handleEndTurn);
    }

    private void handleCardSwap(InteractiveCardComponent sourceCard, InteractiveCardComponent targetCard) {
        GameState currentState = gameLoopManager.getCurrentState();

        if (currentState == null || currentState.gameEnd()) {
            System.out.println("Cannot swap cards: game not running or ended");
            return;
        }

        // Check if EXCHANGE_PRESENT_CARD_ORDER is available
        boolean canSwap = abilityActivationManager.canActivateAbility(
                sourceCard.getCard(),
                AbilitySpec.EXCHANGE_PRESENT_CARD_ORDER,
                currentState,
                new HashSet<>()
        );

        if (!canSwap) {
            System.out.println("Cannot swap cards: EXCHANGE_PRESENT_CARD_ORDER not available");
            return;
        }

        // Find the indices of the source and target cards in the present area
        List<Card> presentCards = currentState.present().cards();
        int sourceIndex = -1;
        int targetIndex = -1;

        for (int i = 0; i < presentCards.size(); i++) {
            Card presentCard = presentCards.get(i);
            if (presentCard.equals(sourceCard.getCard())) {
                sourceIndex = i;
            }
            if (presentCard.equals(targetCard.getCard())) {
                targetIndex = i;
            }
        }

        if (sourceIndex == -1 || targetIndex == -1) {
            System.out.println("❌ Could not find card indices for swap");
            return;
        }

        System.out.println("🔄 Swapping cards " + sourceCard.getCard().number() + " and " + targetCard.getCard().number()
                + " at indices " + sourceIndex + " and " + targetIndex);

        // Pre-select the card indices for the decision provider
        decisionProvider.setPreSelectedCardIndices(List.of(sourceIndex, targetIndex));

        // Execute the swap through the game loop
        boolean success = gameLoopManager.executeManualAbility(AbilitySpec.EXCHANGE_PRESENT_CARD_ORDER);

        if (success) {
            System.out.println("✅ Card swap executed successfully");
            // UI will be updated automatically via notifyStateUpdate
        } else {
            System.out.println("❌ Failed to execute card swap");
            // Clear pre-selected indices if the ability failed
            decisionProvider.setPreSelectedCardIndices(null);
        }
    }

    private void setupGameLoop() {
        // Set up UI synchronization when game state changes
        gameLoopManager.addStateUpdateListener(state -> {
            uiSynchronizer.updateUI(state);
            System.out.println("🔄 Game state updated: " + getGameStateInfo(state));
        });

        // Start the game
        gameLoopManager.startGame();
    }

    private void handleCardActivation(InteractiveCardComponent cardComponent) {
        Card card = cardComponent.getCard();
        GameState currentState = gameLoopManager.getCurrentState();

        if (currentState == null || currentState.gameEnd()) {
            System.out.println("Cannot activate card: game not running or ended");
            return;
        }

        // Find the index of this card in the present area
        List<Card> presentCards = currentState.present().cards();
        int cardIndex = -1;
        for (int i = 0; i < presentCards.size(); i++) {
            if (presentCards.get(i).equals(card)) {
                cardIndex = i;
                break;
            }
        }

        if (cardIndex == -1) {
            System.out.println("Card not found in present area");
            return;
        }

        // Get available abilities for this card
        List<AbilitySpec> availableAbilities = abilityActivationManager.getAvailableAbilities(
                card, currentState, new HashSet<>()
        );

        if (availableAbilities.isEmpty()) {
            System.out.println("Card " + card.number() + " has no available abilities");
            cardComponent.setCandyActivated(false);
            return;
        }

        // For now, activate the primary ability (first in list)
        AbilitySpec primaryAbility = availableAbilities.get(0);

        System.out.println("🎯 Activating " + primaryAbility + " on card " + card.number() + " at index " + cardIndex);

        // Pre-select this card as the ability provider
        decisionProvider.setPreSelectedAbilityProviderIndex(cardIndex);

        boolean success = gameLoopManager.executeManualAbility(primaryAbility);

        if (success) {
            System.out.println("✅ Ability " + primaryAbility + " executed successfully");
            // Visual feedback will be handled by UI synchronizer
        } else {
            System.out.println("❌ Failed to execute ability " + primaryAbility);
            cardComponent.setCandyActivated(false);
            // Clear pre-selected provider if the ability failed
            decisionProvider.setPreSelectedAbilityProviderIndex(null);
        }
    }


    private void handleEndTurn() {
        System.out.println("🔚 End Turn button clicked");
        gameLoopManager.endTurn();
    }

    /**
     * Get readable info about current game state.
     */
    private String getGameStateInfo(GameState state) {
        return String.format("Present: %d cards, Past: %d cards, Finished: %d cards, Candy: %d, Coffee: %d",
            state.present().cards().size(),
            state.past().cards().size(),
            state.finishedPile().cards().size(),
            state.activeStash().candy(),
            state.activeStash().coffee());
    }

    public GameRootLayout getRootLayout() {
        return rootLayout;
    }

    public DimensionService getDimensionService() {
        return dimensionService;
    }

    public UIDecisionProvider getDecisionProvider() {
        return decisionProvider;
    }
}
