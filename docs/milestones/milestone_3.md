# Milestone 3 — Engine Wiring & App Shell

This milestone focuses on integrating the core game engine with the JavaFX UI, establishing the ability execution pipeline, and creating a robust game loop that maintains immutable GameState.

## Overview

Milestone 3 bridges the gap between the visual UI components (Milestone 2) and the game logic engine. It establishes the architectural foundation for ability execution, state management, and user interaction workflows that will support both desktop and JPro web deployment.

## Steps

### 14. App Bootstrap and Full-Screen Shell

**Objective**: Create a robust JavaFX application that provides a full-screen gaming experience with responsive layout support.

**Requirements**:
- Full-screen launch by default with window controls accessible
- Dynamic resizing support that maintains game proportions
- Cross-platform compatibility (desktop + JPro web)
- No hardcoded pixel values - all sizing through DimensionService
- Proper scene graph setup and CSS integration

**Implementation Details**:
- Extend the existing `FinishedFxApp` to implement full-screen behavior
- Configure primary stage with appropriate window properties
- Integrate DimensionService binding to scene dimensions
- Add window resize handlers that maintain UI responsiveness
- Ensure proper initialization order for all UI components
- Add JPro compatibility annotations and configurations

**Acceptance Criteria**:
- Application launches in full-screen mode by default
- ESC key or Alt+Enter toggles full-screen/windowed mode
- All UI elements scale proportionally during window resize
- Works identically on desktop JavaFX and JPro web deployment
- No visual glitches during resize operations
- Maintains game aspect ratio constraints

### 15. GameState and Ability System Analysis

**Objective**: Thoroughly analyze and document the complete ability system defined in `AbilitySpec` to understand all game mechanics.

**Requirements**:
- Review all abilities in `com.adrian.finished.model.abilities.AbilitySpec`
- Document ability descriptions, parameters, and interactions
- Identify decision points that require UI input
- Map abilities to their trigger conditions and effects
- Understand state transitions and immutability requirements

**Implementation Details**:
- Examine each ability class and its implementation requirements
- Document ability categories (instant, choice-based, conditional)
- Identify abilities that require DecisionProvider input
- Map ability interdependencies and execution order
- Document GameState fields affected by each ability
- Create reference documentation for ability behaviors

**Deliverables**:
- Complete ability reference documentation
- GameState field mapping per ability
- Decision point identification matrix
- Ability execution flow diagrams
- Integration requirements analysis

### 16. Ability Pipeline Integration

**Objective**: Establish the ability execution pipeline that maintains immutable GameState and publishes UI update events.

**Requirements**:
- Implement `AbilityExecutors.withListeners(...)` wrapper pattern
- Create event publishing system for UI updates
- Maintain GameState immutability throughout execution
- Handle ability execution errors gracefully
- Support ability chaining and conditional execution

**Implementation Details**:

**AbilityExecutor Pipeline Setup**:
```java
// Create wrapped executor pipeline
AbilityExecutor pipeline = AbilityExecutors.withListeners(
    baseExecutor,
    new UIUpdateListener(),
    new GameLogListener(),
    new StateValidationListener()
);

// Execute abilities with state replacement
GameState newState = pipeline.execute(currentState, ability, context);
gameStateManager.replaceState(newState);
```

**Event Publishing System**:
- Create listener interfaces for different event types
- Implement event dispatching for UI updates
- Add state change notifications
- Support for async UI updates without blocking game logic

**State Management**:
- Ensure all GameState modifications return new instances
- Implement state validation between ability executions
- Add rollback capability for failed ability executions
- Maintain execution history for debugging

**Error Handling**:
- Graceful handling of ability execution failures
- User-friendly error messages for invalid moves
- State consistency verification after each ability
- Logging and debugging support

**Acceptance Criteria**:
- All abilities execute through the pipeline wrapper
- UI receives immediate updates after ability execution
- GameState remains immutable throughout the process
- Failed abilities don't corrupt game state
- Multiple listeners can be attached to the pipeline
- Execution events are properly sequenced and delivered

### 17. DecisionProvider Adapter (UI-Driven)

**Objective**: Implement a DecisionProvider that seamlessly integrates UI workflows for collecting user choices required by abilities.

**Requirements**:
- Block ability execution pending user input
- Support all decision types required by abilities
- Work identically on desktop and JPro
- Provide intuitive UI for each decision type
- Handle user cancellation and invalid inputs
- Maintain responsive UI during decision collection

**Implementation Details**:

**Core DecisionProvider Interface**:
```java
public class UIDecisionProvider implements DecisionProvider {
    
    @Override
    public Card chooseCardFromList(List<Card> options, String prompt) {
        // Show inline card selection overlay
        // Block until user selects or cancels
        // Return selected card or null for cancellation
    }
    
    @Override
    public List<Card> chooseCardsFromList(List<Card> options, int min, int max, String prompt) {
        // Multi-card selection interface with inline controls
        // Enforce min/max constraints
        // Visual feedback for selection count
        // Interactive highlight and selection states
    }
    
    @Override
    public int chooseNumber(int min, int max, String prompt) {
        // Inline number picker or slider interface
        // Validate range constraints
        // Show current value feedback
        // Direct manipulation controls
    }
}
```

**UI Integration Patterns**:
- Inline selection overlays for card choices
- Hover previews for card options
- Animated transitions between decision states
- Progress indicators for multi-step decisions
- Interactive highlight states for selectable elements
- Contextual information panels for decision guidance

**Cross-Platform Compatibility**:
- Responsive design that works on desktop and web
- Touch-friendly controls for web deployment
- Keyboard navigation support
- Accessibility compliance (screen readers, high contrast)

**Decision Flow Management**:
- Queue multiple decisions when needed
- Handle nested decision requirements
- Provide decision history and undo capability
- Save decision preferences where appropriate

**Acceptance Criteria**:
- All AbilitySpec decision methods are implemented
- UI blocks appropriately during decision collection
- User can cancel decisions without breaking game state
- Decision UI is intuitive and self-explanatory
- Works identically on desktop JavaFX and JPro web
- Performance remains smooth during decision workflows
- Error states are handled gracefully

### 18. Game Loop Integration

**Objective**: Implement a complete game loop that orchestrates GameState management, ability execution, and UI synchronization.

**Requirements**:
- Integrate with current JavaFX UI architecture
- Maintain GameState as the single source of truth
- Handle turn-based game progression
- Support real-time UI updates
- Manage game initialization and cleanup
*
**Implementation Details**:

**Game Loop Controller**:
```java
public class GameLoopController {
    private GameState currentState;
    private final AbilityExecutor abilityPipeline;
    private final UIDecisionProvider decisionProvider;
    private final GameStateManager stateManager;
    
    public void initializeNewGame() {
        // Create initial GameState
        // Set up UI with initial state
        // Begin first turn
    }
    
    public void processPlayerAction(PlayerAction action) {
        // Validate action against current state
        // Execute through ability pipeline
        // Update GameState immutably
        // Refresh UI with new state
        // Check for game end conditions
    }
    
    public void advanceTurn() {
        // Process end-of-turn abilities
        // Advance turn counter
        // Reset turn-specific state
        // Begin next player's turn
    }
}
```

**State Synchronization**:
- Bind UI components to GameState properties
- Implement efficient state diff algorithms
- Update only changed UI elements
- Maintain smooth animations during state transitions

**Game Flow Management**:
- Handle game initialization sequence
- Manage turn progression and player switching
- Process automatic abilities and triggers
- Detect and handle game end conditions

**Integration with Existing UI**:
- Connect GameController with GameLoopController
- Update area layouts based on GameState changes
- Synchronize card displays with state model
- Maintain existing drag-and-drop interactions



**Acceptance Criteria**:
- Complete game can be played from start to finish
- All UI updates reflect GameState changes accurately
- Turn progression works smoothly
- Error recovery maintains game integrity
- Performance is smooth throughout entire game session
- Integration maintains existing UI responsiveness

## Success Criteria

At the completion of Milestone 3:

1. **Full Application Functionality**: The game launches in full-screen mode with complete UI responsiveness
2. **Complete Ability Integration**: All abilities from AbilitySpec can be executed through the pipeline
3. **Seamless UI Decisions**: Users can make all required decisions through intuitive UI workflows
4. **Robust Game Loop**: Complete games can be played with proper state management
5. **Cross-Platform Ready**: Application works identically on desktop and JPro web
6. **Performance Optimized**: Smooth gameplay with efficient state updates and UI rendering

## Dependencies

- Milestone 1: Core game engine and ability system
- Milestone 2: Complete UI component library
- JavaFX 21+ for desktop compatibility
- JPro 2024.1+ for web deployment
- Existing DimensionService and layout architecture

## Deliverables

1. Enhanced `FinishedFxApp` with full-screen capability
2. Complete `UIDecisionProvider` implementation
3. `AbilityExecutors` pipeline with event publishing
4. `GameLoopController` with state management
5. Integration layer connecting UI to game engine
6. Cross-platform deployment configurations
7. Comprehensive ability system documentation
