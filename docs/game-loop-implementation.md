# Game Loop Implementation - Complete Integration Guide

## Overview
This document describes the complete game loop implementation for the Finished! card game, integrating ability executors from the core package with the UI system to create a fully functional game experience.

## Architecture

### Core Components

#### 1. GameLoopManager (`/ui/pipeline/GameLoopManager.java`)
**Purpose**: Orchestrates the complete game loop sequence as defined in ability-system-analysis.md

**Key Features**:
- Manages game state transitions through all ability phases
- Executes automatic abilities in correct order (0-8)
- Handles manual ability activation during USER_INPUT_REQUIRED phase
- Tracks used abilities per turn to enforce usage limits
- Provides state update notifications for UI synchronization

**Game Loop Sequence**:
```
1. BEGIN_TURN (order 0) - Draw 3 cards or resolve future area
   ↓
2. TAKE_CANDY (order 1) - Auto-gain candy from take-candy cards  
   ↓
3. SCORE_CARD (order 2) - Auto-score sequential cards (loops until no more scoring)
   ↓
4. USER_INPUT_REQUIRED phase - Manual abilities (order 3) - User interaction
   ↓
5. END_TURN_BEGIN (order 4) - Move present→past, return candy
   ↓
6. SEQUENCE_RULE (order 5) - Award candy for sequences
   ↓
7. DRINK_COFEE (order 6) - Spend coffee if card 48 in past
   ↓
8. END_TURN_END (order 8) - Keep 3 cards in past, move rest to draw stack
   ↓
Loop back to BEGIN_TURN or Game End
```

#### 2. UIGameStateSynchronizer (`/ui/pipeline/UIGameStateSynchronizer.java`)
**Purpose**: Keeps UI components synchronized with GameState changes

**Responsibilities**:
- Updates PresentAreaLayout with current cards from GameState.present()
- Updates PastAreaLayout with most recent past card
- Updates FutureAreasLayout with next future area cards
- Updates ActiveStashLayout with candy/coffee counts
- Updates FinishedPileOverlay with highest scored card
- Handles game end victory/defeat display

#### 3. AbilityActivationManager (`/ui/pipeline/AbilityActivationManager.java`)
**Purpose**: Maps cards to their available abilities based on AbilitySpec definitions

**Card→Ability Mapping**:
- Card 2 → DRAW_TWO (1 candy)
- Cards 5,11,17,23,25,41 → CARDS_INTO_PAST (1 candy)
- Cards 8,18,30,44 → CARDS_FROM_PAST (1 candy)
- Cards 9,14,20,27,31,34,46 → DRAW_ONE (1 candy)
- Cards 12,19,32,40 → CARD_INTO_FUTURE (1 candy)
- Cards 13,22,33,39,43 → EXCHANGE_CARD (1 candy)
- Card 47 → DRAW_ONE_3X (1 candy each, up to 3 uses)
- Other cards → EXCHANGE_PRESENT_CARD_ORDER (free)

**Validation**:
- Checks if ability already used this turn
- Verifies card has required candy (abilitiesTriggered > 0)
- Ensures player has candy in active stash
- Validates specific ability prerequisites (e.g., enough cards in past)

### Integration Points

#### GameController Integration
**Updated GameController** (`/ui/controller/GameController.java`):
- Initializes all game loop components
- Sets up UI synchronization callbacks
- Handles card activation events by delegating to AbilityActivationManager
- Handles end turn button clicks by calling GameLoopManager.endTurn()
- Starts the game automatically on initialization

**Key Methods**:
```java
private void handleCardActivation(InteractiveCardComponent cardComponent)
// → Gets available abilities for card
// → Executes primary ability via GameLoopManager
// → UI updates automatically via synchronizer

private void handleEndTurn()
// → Calls GameLoopManager.endTurn()
// → Triggers END_TURN_BEGIN → SEQUENCE_RULE → DRINK_COFEE → END_TURN_END sequence
// → Starts next turn automatically
```

#### ActiveStashLayout Enhancements
**New Methods Added**:
- `setEndTurnCallback(Runnable callback)` - Connects End Turn button to game loop
- `updateStash(int candy, int coffee)` - Updates visual display from GameState
- `setEndTurnEnabled(boolean enabled)` - Controls button availability

#### UI Component Updates
**PresentAreaLayout**: Uses existing methods (`setCards`, `getCurrentCards`)
**PastAreaLayout**: Uses existing method (`setPastCard`)
**FutureAreasLayout**: Uses existing method (`setFutureCard`)
**FinishedPileOverlay**: Uses existing method (`setFinishedCard`)

### Real Game Data Flow

#### Initialization Sequence
1. **GameController Constructor**:
   - Creates GameLoopManager with UIDecisionProvider
   - Sets up UIGameStateSynchronizer
   - Registers state update listener
   - Calls `gameLoopManager.startGame()`

2. **Game Startup**:
   - GameLoopManager executes BEGIN_GAME ability
   - Creates shuffled deck with cards 1-47 + card 48 at bottom
   - Sets initial stash: 0 candy, 5 coffee (active), 10 candy, 0 coffee (reserved)
   - UI synchronizer updates all areas with initial empty state

3. **First Turn**:
   - BEGIN_TURN draws 3 cards to present
   - TAKE_CANDY checks for take-candy symbols and awards candy
   - SCORE_CARD attempts to score card 1 (if present)
   - UI shows real cards based on actual game state
   - User can now interact with cards

#### User Interaction Flow
1. **Card Activation**:
   ```
   User clicks card → handleCardActivation()
   → AbilityActivationManager.getAvailableAbilities()
   → GameLoopManager.executeManualAbility()
   → Core executor (e.g., DrawTwoExecutor) applies changes
   → UI synchronizer updates display
   → TAKE_CANDY and SCORE_CARD run automatically
   ```

2. **End Turn**:
   ```
   User clicks End Turn → handleEndTurn()
   → GameLoopManager.endTurn()
   → END_TURN_BEGIN: present cards → past, candy returned
   → SEQUENCE_RULE: award candy for sequences
   → DRINK_COFEE: spend coffee if card 48 in past
   → END_TURN_END: keep 3 past cards, rest → draw stack
   → Next turn: BEGIN_TURN draws 3 new cards
   ```

### Decision Provider Integration

The UIDecisionProvider system is fully integrated and will be automatically invoked when abilities require user decisions:

**CARDS_INTO_PAST Ability**:
- User activates ability on eligible card (5,11,17,23,25,41)
- DecisionProvider shows card selection overlay
- User selects 2 cards from present area
- CardsIntoPastExecutor moves selected cards to past, draws replacements

**DRAW_TWO Ability**:
- User activates ability on card 2
- If multiple card 2s present, DecisionProvider shows ability provider selection
- DrawTwoExecutor draws 2 cards from stack (or past if empty)

**DRAW_ONE_3X Ability**:
- User activates ability on card 47
- DecisionProvider shows number selection overlay (1-3)
- DrawOne3xExecutor executes the ability selected number of times

### Game State Fidelity

**No More Mock Data**: All cards displayed are real cards from the actual GameState managed by ability executors.

**True Game Loop**: The system follows the exact ability execution order specified in the game rules.

**State Consistency**: UI is always synchronized with the authoritative GameState from the core game engine.

**Decision Integration**: User decisions are collected through polished UI overlays and passed directly to ability executors.

### Victory/Defeat Conditions

**Victory**: When card 48 is scored (moved to finished pile)
- SCORE_CARD executor moves card 48 to finished pile
- GAME_END_WIN executor sets gameEnd=true
- UI synchronizer detects win condition and displays victory message

**Defeat**: When card 48 enters past area with no coffee
- END_TURN_BEGIN moves card 48 to past
- DRINK_COFEE executor checks coffee availability
- If no coffee: GAME_END_LOSE executor sets gameEnd=true
- UI synchronizer detects lose condition and displays defeat message

### Performance and Threading

**JavaFX Thread Safety**: All UI updates happen on JavaFX Application Thread via Platform.runLater()

**Non-Blocking UI**: Decision overlays block ability execution but keep UI responsive

**Efficient Updates**: Only changed UI components are updated when GameState changes

### Cross-Platform Compatibility

**Desktop JavaFX**: Full mouse and keyboard interaction with all overlays and controls

**JPro Web**: Touch-friendly decision overlays, responsive layout, identical game behavior

## Testing and Validation

### How to Test

1. **Start Application**: GameController automatically starts a new game
2. **Observe Real Cards**: Present area shows 3 real cards from shuffled deck
3. **Test Take Candy**: Cards 3,6,10,15,21,28,36,45 should show candy gain
4. **Test Scoring**: Card 1 should auto-score if drawn
5. **Test Manual Abilities**: Click cards to activate their abilities
6. **Test Decision Overlays**: Cards requiring decisions should show UI overlays
7. **Test End Turn**: Click End Turn to trigger turn sequence
8. **Test Game End**: Play until card 48 appears for victory/defeat

### Expected Behavior

- **Real Deck**: Cards appear in random order from properly shuffled deck
- **Automatic Abilities**: Take candy, scoring, and turn management happen automatically
- **Manual Abilities**: User can activate card abilities by clicking
- **Decision UI**: Professional overlays collect user choices for complex abilities
- **State Synchronization**: All UI areas update automatically when game state changes
- **Turn Flow**: Complete turn sequence executes when End Turn is clicked
- **Game End**: Victory/defeat conditions properly detected and displayed

## Implementation Status

✅ **Complete Game Loop**: All ability phases implemented and integrated
✅ **Real Game State**: No mock data, all cards from actual game state
✅ **Ability Executors**: All 21 abilities from core package integrated
✅ **Decision Provider**: Full UI decision collection system
✅ **UI Synchronization**: All areas update automatically from game state
✅ **Turn Management**: Proper turn sequence with end turn functionality
✅ **Victory/Defeat**: Game end conditions properly handled
✅ **Cross-Platform**: Works on desktop and JPro web deployment

The game loop implementation is complete and ready for testing. Players can now experience the full Finished! card game with professional UI, real game mechanics, and complete ability system integration.
