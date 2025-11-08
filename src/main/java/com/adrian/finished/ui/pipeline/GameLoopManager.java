package com.adrian.finished.ui.pipeline;

import com.adrian.finished.core.*;
import com.adrian.finished.model.*;
import com.adrian.finished.model.abilities.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Manages the complete game loop as described in ability-system-analysis.md.
 * Executes abilities in the correct order and handles transitions between
 * automatic abilities and user input phases.
 * <p>
 * Game Loop Sequence:
 * 1. BEGIN_TURN (order 0)
 * 2. TAKE_CANDY (order 1) - if applicable cards present
 * 3. SCORE_CARD (order 2) - if scoreable cards present
 * 4. USER_INPUT_REQUIRED phase - manual abilities (order 3)
 * 5. END_TURN_BEGIN (order 4)
 * 6. SEQUENCE_RULE (order 5)
 * 7. DRINK_COFEE (order 6) - if card 48 in past
 * 8. END_TURN_END (order 8)
 * Loop back to BEGIN_TURN or Game End
 */
public class GameLoopManager {

    private GameState currentState;
    private final UIDecisionProvider decisionProvider;
    private final Map<AbilitySpec, AbilityExecutor> executors;
    private final List<Consumer<GameState>> stateUpdateListeners = new ArrayList<>();
    private boolean gameRunning = false;

    // Track which manual abilities have been used this turn
    private final Set<AbilitySpec> usedManualAbilities = new HashSet<>();

    public GameLoopManager(UIDecisionProvider decisionProvider) {
        this.decisionProvider = decisionProvider;
        this.executors = createExecutorMap();
    }

    /**
     * Initialize the game with BEGIN_GAME ability.
     */
    public void startGame() {
        if (gameRunning) {
            System.out.println("Game is already running");
            return;
        }

        System.out.println("🎮 Starting new game...");

        // Create initial empty game state
        GameState initialState = createInitialGameState();

        // Execute BEGIN_GAME ability
        AbilityContext beginGameContext = new AbilityContext(initialState, AbilitySpec.BEGIN_GAME, decisionProvider);
        System.out.println("ADRIAN 1");
        currentState = executors.get(AbilitySpec.BEGIN_GAME).apply(beginGameContext);

        gameRunning = true;
        notifyStateUpdate();

        System.out.println("✅ Game initialized, starting first turn...");

        // Start the main game loop
        executeGameLoop();
    }

    /**
     * Execute the main game loop sequence.
     */
    private void executeGameLoop() {
        if (!gameRunning || currentState.gameEnd()) {
            return;
        }

        try {
            // Clear used abilities at start of new turn
            usedManualAbilities.clear();
            System.out.println("ADRIAN 5");
            System.out.println(currentState.gameEnd());
            // 1. BEGIN_TURN (order 0)
            currentState = executeAbility(AbilitySpec.BEGIN_TURN);
//            if (currentState.gameEnd()) return;

            // 2. TAKE_CANDY (order 1) - if applicable cards present
            currentState = executeAbility(AbilitySpec.TAKE_CANDY);
//            if (currentState.gameEnd()) return;

            // 3. SCORE_CARD (order 2) - if scoreable cards present (loop until no more scoring)
            boolean scored;
            do {
                GameState beforeScoring = currentState;
                currentState = executeAbility(AbilitySpec.SCORE_CARD);
                scored = !currentState.equals(beforeScoring);

                // Check for win condition after scoring
                if (currentState.gameEnd()) {
                    if (currentState.finishedPile().cards().stream().anyMatch(card -> card.number() == 48)) {
                        currentState = executeAbility(AbilitySpec.GAME_END_WIN);
                    }
                    return;
                }

                // Take candy for any newly drawn cards after scoring
                if (scored) {
                    currentState = executeAbility(AbilitySpec.TAKE_CANDY);
                }
            } while (scored);

            // 4. USER_INPUT_REQUIRED phase - wait for manual ability activation
            // This phase is handled by user interactions, not automatic execution
            System.out.println("🎯 User input phase - activate abilities by clicking cards or end turn");

        } catch (Exception e) {
            System.err.println("❌ Error in game loop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Execute the end turn sequence when user clicks End Turn button.
     */
    public void endTurn() {
        if (!gameRunning || currentState.gameEnd()) {
            return;
        }

        try {
            System.out.println("🔄 Ending turn...");

            // 5. END_TURN_BEGIN (order 4)
            currentState = executeAbility(AbilitySpec.END_TURN_BEGIN);
            if (currentState.gameEnd()) return;

            // 6. SEQUENCE_RULE (order 5)
            currentState = executeAbility(AbilitySpec.SEQUENCE_RULE);
            if (currentState.gameEnd()) return;

            // 7. DRINK_COFEE (order 6) - if card 48 in past
            currentState = executeAbility(AbilitySpec.DRINK_COFEE);
            if (currentState.gameEnd()) {
                // Check for lose condition
                currentState = executeAbility(AbilitySpec.GAME_END_LOSE);
                return;
            }

            // 8. END_TURN_END (order 8)
            currentState = executeAbility(AbilitySpec.END_TURN_END);
            if (currentState.gameEnd()) return;

            System.out.println("✅ Turn ended, starting next turn...");

            // Loop back to BEGIN_TURN for next turn
            executeGameLoop();

        } catch (Exception e) {
            System.err.println("❌ Error ending turn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Execute a manual ability (triggered by user interaction).
     */
    public boolean executeManualAbility(AbilitySpec ability) {
        if (!gameRunning || currentState.gameEnd()) {
            return false;
        }

        if (!isManualAbility(ability)) {
            System.out.println("❌ " + ability + " is not a manual ability");
            return false;
        }

        // Only check if ability was used for abilities that should be limited per turn
        if (shouldTrackAbilityUsage(ability) && usedManualAbilities.contains(ability)) {
            System.out.println("❌ " + ability + " has already been used this turn");
            return false;
        }

        try {
            System.out.println("🎯 Executing manual ability: " + ability);

            GameState beforeAbility = currentState;
            currentState = executeAbility(ability);

            if (!currentState.equals(beforeAbility)) {
                // Only track abilities that should be limited per turn
                if (shouldTrackAbilityUsage(ability)) {
                    usedManualAbilities.add(ability);
                }

                // After manual abilities, check for TAKE_CANDY and SCORE_CARD
                if (ability == AbilitySpec.EXCHANGE_PRESENT_CARD_ORDER) {
                    System.out.println("✅ Manual ability executed successfully");
                    return true;
                }

                currentState = executeAbility(AbilitySpec.TAKE_CANDY);

                // Try to score cards after drawing new ones
                boolean scored;
                do {
                    GameState beforeScoring = currentState;
                    currentState = executeAbility(AbilitySpec.SCORE_CARD);
                    scored = !currentState.equals(beforeScoring);

                    if (currentState.gameEnd()) {
                        if (currentState.finishedPile().cards().stream().anyMatch(card -> card.number() == 48)) {
                            currentState = executeAbility(AbilitySpec.GAME_END_WIN);
                        }
                        return true;
                    }

                    if (scored) {
                        currentState = executeAbility(AbilitySpec.TAKE_CANDY);
                    }
                } while (scored);

                System.out.println("✅ Manual ability executed successfully");
                return true;
            } else {
                System.out.println("ℹ️ Manual ability had no effect");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error executing manual ability " + ability + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Determine if an ability should be tracked for per-turn usage limits.
     * Some abilities like EXCHANGE_PRESENT_CARD_ORDER can be used multiple times per turn.
     */
    private boolean shouldTrackAbilityUsage(AbilitySpec ability) {
        switch (ability) {
            case EXCHANGE_PRESENT_CARD_ORDER:
                // This ability can be used multiple times per turn - don't track usage
                return false;

            // Special case: DRAW_ONE_3X can be used up to 3 times, but needs special handling
            case DRAW_ONE_3X:
                // This needs special per-card tracking, not global turn tracking
                return false;

            case DRAW_ONE:
                return false;

            case CARDS_FROM_PAST:
                return false;

            default:
                // Most abilities are limited to once per turn
                return true;
        }
    }



    /**
     * Execute a specific ability and update state.
     */
    private GameState executeAbility(AbilitySpec ability) {
        AbilityExecutor executor = executors.get(ability);
        if (executor == null) {
            System.out.println("⚠️ No executor found for ability: " + ability);
            return currentState;
        }

        AbilityContext context = new AbilityContext(currentState, ability, decisionProvider);
        GameState newState = executor.apply(context);

        System.out.println("ADRIAN 6");
        if (!newState.equals(currentState)) {
            System.out.println("ADRINA 7");
            System.out.println("🔄 " + ability + " executed - state updated");
            currentState = newState; // Update the field BEFORE notifying

            notifyStateUpdate(); // Now this uses the correct updated state
        }

        return newState;
    }


    /**
     * Check if an ability is a manual ability that requires user activation.
     */
    private boolean isManualAbility(AbilitySpec ability) {
        return ability.phase() == AbilityPhase.USER_INPUT_REQUIRED;
    }

    /**
     * Add a listener for game state updates.
     */
    public void addStateUpdateListener(Consumer<GameState> listener) {
        stateUpdateListeners.add(listener);
    }

    /**
     * Notify all listeners of a state update.
     */
    private void notifyStateUpdate() {
        for (Consumer<GameState> listener : stateUpdateListeners) {
            try {
                listener.accept(currentState);
            } catch (Exception e) {
                System.err.println("Error notifying state update listener: " + e.getMessage());
            }
        }
    }

    /**
     * Create the initial empty game state.
     */
    private GameState createInitialGameState() {
        return new GameState(
            new Stash(0, 5), // activeStash - start with 5 coffee, no candy
            new Stash(10, 0), // reservedStash - 10 candy available
            new DrawStack(new ArrayDeque<>()), // empty initially
            new PresentArea(List.of()),
            new PastArea(new ArrayDeque<>()),
            List.of(), // futureAreas
            new FinishedPile(List.of()),
            0, // activeAllCardsInFutureAreas
            false // gameEnd
        );
    }

    /**
     * Create the map of ability executors.
     */
    private Map<AbilitySpec, AbilityExecutor> createExecutorMap() {
        Map<AbilitySpec, AbilityExecutor> map = new HashMap<>();

        // Automatic abilities
        map.put(AbilitySpec.BEGIN_GAME, new BeginGameExecutor());
        map.put(AbilitySpec.BEGIN_TURN, new BeginTurnExecutor());
        map.put(AbilitySpec.TAKE_CANDY, new TakeCandyExecutor());
        map.put(AbilitySpec.SCORE_CARD, new ScoreCardExecutor());
        map.put(AbilitySpec.END_TURN_BEGIN, new EndTurnBeginExecutor());
        map.put(AbilitySpec.SEQUENCE_RULE, new SequenceRuleExecutor());
        map.put(AbilitySpec.DRINK_COFEE, new DrinkCofeeExecutor());
        map.put(AbilitySpec.END_TURN_END, new EndTurnEndExecutor());
        map.put(AbilitySpec.GAME_END_WIN, new GameEndWinExecutor());
        map.put(AbilitySpec.GAME_END_LOSE, new GameEndLoseExecutor());

        // Manual abilities
        map.put(AbilitySpec.DRAW_TWO, new DrawTwoExecutor());
        map.put(AbilitySpec.CARDS_INTO_PAST, new CardsIntoPastExecutor());
        map.put(AbilitySpec.DRAW_ONE, new DrawOneExecutor());
        map.put(AbilitySpec.DRAW_ONE_3X, new DrawOne3xExecutor());
        map.put(AbilitySpec.EXCHANGE_CARD, new ExchangeCardExecutor());
        map.put(AbilitySpec.CARDS_FROM_PAST, new CardsFromPastExecutor());
        map.put(AbilitySpec.CARD_INTO_FUTURE, new CardIntoFutureExecutor());
        map.put(AbilitySpec.EXCHANGE_PRESENT_CARD_ORDER, new ExchangePresentCardOrderExecutor());
        map.put(AbilitySpec.RESET_CANDIES, new ResetCandiesExecutor());

        return map;
    }

    // Getters
    public GameState getCurrentState() {
        return currentState;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void stopGame() {
        gameRunning = false;
        System.out.println("🛑 Game stopped");
    }
}
