# Ability System Analysis - Complete Reference Documentation

## Overview

This document provides a comprehensive analysis of the Finished! card game ability system as defined in `AbilitySpec.java`. The system consists of 21 distinct abilities organized into automatic and manual categories, each with specific triggers, effects, and decision requirements.

## Ability Categories

### Automatic Abilities (11 abilities)
Abilities that trigger automatically based on game state or phase transitions without user input.

### Manual Abilities (10 abilities) 
Abilities that require user input and decision-making, typically activated by spending candy tokens.

## Complete Ability Reference

### Automatic Abilities

#### 1. BEGIN_GAME
- **ID**: `beginGame`
- **Phase**: `GAME_START`
- **Order**: -1 (First)
- **Candy Required**: No
- **Cards**: None (applies to game setup)
- **Description**: Initialize game by moving all cards to draw deck, shuffle cards 1-47, place card 48 at bottom
- **GameState Effects**: 
  - `drawStack`: Reset and populate with shuffled cards 1-47 + card 48 at bottom
  - `present`, `past`, `futureAreas`, `finishedPile`: Clear all areas
- **Next Abilities**: [0] - BEGIN_TURN
- **Decision Points**: None

#### 2. BEGIN_TURN  
- **ID**: `beginTurn`
- **Phase**: `TURN_START`
- **Order**: 0
- **Candy Required**: No
- **Cards**: None
- **Description**: Draw 3 cards to present OR resolve first future area to present (if `activeAllCardsInFutureAreas > 0`)
- **GameState Effects**:
  - `present`: Add 3 new cards OR move future area cards
  - `drawStack`: Remove cards if drawing from stack
  - `futureAreas`: Remove first area if resolving future
  - `activeAllCardsInFutureAreas`: Decrement by 1 if resolving future
- **Next Abilities**: [1] - TAKE_CANDY
- **Decision Points**: None

#### 3. TAKE_CANDY
- **ID**: `takeCandy`
- **Phase**: `TURN_START`
- **Order**: 1
- **Candy Required**: No
- **Cards**: [3, 6, 10, 15, 21, 28, 36, 45] (8 cards with takeCandy symbol)
- **Description**: Gain 1 candy when takeCandy cards enter present from draw stack (not from past/future)
- **GameState Effects**:
  - `activeStash`: Increase candy by 1
  - `reservedStash`: Decrease candy by 1
- **Next Abilities**: [2] - SCORE_CARD
- **Decision Points**: None

#### 4. SCORE_CARD
- **ID**: `scoreCard`
- **Phase**: `TURN_START`
- **Order**: 2
- **Candy Required**: No
- **Cards**: None (applies to cards 1-47)
- **Description**: Automatically score next sequential card to sorted pile, draw replacement
- **GameState Effects**:
  - `finishedPile`: Add sequentially correct card
  - `present`: Remove scored card, add replacement from draw
  - `drawStack`: Remove replacement card
- **Next Abilities**: [1, 9] - TAKE_CANDY or GAME_END_WIN
- **Decision Points**: None

#### 5. START
- **ID**: `start`
- **Phase**: `TURN_START`
- **Order**: 2
- **Candy Required**: No
- **Cards**: [1] (Card 1 only)
- **Description**: Special scoring behavior for card 1 (starting card)
- **GameState Effects**: Same as SCORE_CARD but specific to card 1
- **Next Abilities**: [1] - TAKE_CANDY
- **Decision Points**: None

#### 6. END_TURN_BEGIN
- **ID**: `endTurnBegin`
- **Phase**: `TURN_END`
- **Order**: 4
- **Candy Required**: No
- **Cards**: None
- **Description**: Move present cards to past, return their candy to reserved stash
- **GameState Effects**:
  - `present`: Clear all cards
  - `past`: Add all present cards
  - `reservedStash`: Increase by candy removed from cards
  - Card candy counters: Reset to 0
- **Next Abilities**: [5] - SEQUENCE_RULE
- **Decision Points**: None

#### 7. SEQUENCE_RULE
- **ID**: `sequenceRule`
- **Phase**: `TURN_END`
- **Order**: 5
- **Candy Required**: No
- **Cards**: None
- **Description**: Award candy for ascending sequences of 3+ cards moved to past
- **GameState Effects**:
  - `activeStash`: Increase by (sequence_length - 1) per sequence
  - `reservedStash`: Decrease correspondingly
- **Next Abilities**: [6] - DRINK_COFEE
- **Decision Points**: None

#### 8. DRINK_COFEE
- **ID**: `drinkCofee`
- **Phase**: `TURN_END`
- **Order**: 6
- **Candy Required**: No
- **Cards**: [48] (Card 48 only)
- **Description**: Spend 1 coffee when card 48 enters past, lose if no coffee available
- **GameState Effects**:
  - Coffee tokens: Decrease by 1 (if available)
  - `gameEnd`: Set to true if no coffee tokens
- **Next Abilities**: [8, 10] - END_TURN_END or GAME_END_LOSE
- **Decision Points**: None

#### 9. END_TURN_END
- **ID**: `endTurnEnd`
- **Phase**: `TURN_END`
- **Order**: 8
- **Candy Required**: No
- **Cards**: None
- **Description**: Keep only 3 cards in past, move oldest under draw stack
- **GameState Effects**:
  - `past`: Keep only 3 most recent cards
  - `drawStack`: Add oldest past cards to bottom
- **Next Abilities**: [0] - BEGIN_TURN (next turn)
- **Decision Points**: None

#### 10. GAME_END_WIN
- **ID**: `gameEndWin`
- **Phase**: `GAME_END_VICTORY`
- **Order**: 9
- **Candy Required**: No
- **Cards**: None
- **Description**: Victory condition when card 48 is scored
- **GameState Effects**:
  - `gameEnd`: Set to true
  - Victory state: Player wins
- **Next Abilities**: [-1] - Game Over
- **Decision Points**: None

#### 11. GAME_END_LOSE
- **ID**: `gameEndLose`
- **Phase**: `GAME_END_LOSE`
- **Order**: 10
- **Candy Required**: No
- **Cards**: None
- **Description**: Defeat condition when card 48 enters past with no coffee
- **GameState Effects**:
  - `gameEnd`: Set to true
  - Victory state: Player loses
- **Next Abilities**: [-1] - Game Over
- **Decision Points**: None

### Manual Abilities (Require User Input)

#### 12. DRAW_TWO
- **ID**: `drawTwo`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy)
- **Cards**: [2] (Card 2 only)
- **Limit**: 1 use per turn
- **Description**: Draw 2 additional cards from draw pile (or past if draw empty)
- **GameState Effects**:
  - `present`: Add 2 cards
  - `drawStack` or `past`: Remove 2 cards
  - `activeStash`: Decrease by 1 candy
- **Next Abilities**: [1] - TAKE_CANDY (for new cards)
- **Decision Points**: 
  - **Activation**: User chooses to activate ability
  - **Source Selection**: Choose draw pile vs past area if draw empty

#### 13. CARDS_INTO_PAST
- **ID**: `cardsIntoPast`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy)
- **Cards**: [5, 11, 17, 23, 25, 41] (6 cards)
- **Limit**: 1 use per turn
- **Description**: Move 2 selected present cards to past, draw 2 replacements
- **GameState Effects**:
  - `present`: Remove 2 selected cards, add 2 new cards
  - `past`: Add 2 selected cards
  - `drawStack`: Remove 2 replacement cards
  - `activeStash`: Decrease by 1 candy
- **Next Abilities**: [1] - TAKE_CANDY
- **Decision Points**:
  - **Activation**: User chooses to activate ability
  - **Card Selection**: User selects 2 cards from present area

#### 14. DRAW_ONE
- **ID**: `drawOne`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy)
- **Cards**: [9, 14, 20, 27, 31, 34, 46] (7 cards)
- **Limit**: 1 use per turn
- **Description**: Draw 1 additional card from draw pile (or past if draw empty)
- **GameState Effects**:
  - `present`: Add 1 card
  - `drawStack` or `past`: Remove 1 card
  - `activeStash`: Decrease by 1 candy
- **Next Abilities**: [1] - TAKE_CANDY
- **Decision Points**:
  - **Activation**: User chooses to activate ability
  - **Source Selection**: Choose draw pile vs past area if draw empty

#### 15. DRAW_ONE_3X
- **ID**: `drawOne3x`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy per use)
- **Cards**: [47] (Card 47 only)
- **Limit**: 3 uses per turn
- **Description**: Draw 1 card per candy spent, up to 3 times
- **GameState Effects** (per activation):
  - `present`: Add 1 card
  - `drawStack` or `past`: Remove 1 card
  - `activeStash`: Decrease by 1 candy
- **Next Abilities**: [1] - TAKE_CANDY
- **Decision Points**:
  - **Activation Count**: User chooses how many times to activate (1-3)
  - **Source Selection**: Choose draw pile vs past area if draw empty (per activation)

#### 16. EXCHANGE_CARD
- **ID**: `exchangeCard`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy)
- **Cards**: [13, 22, 33, 39, 43] (5 cards)
- **Limit**: 1 use per turn
- **Description**: Draw 1 card to present, then place any present card on top of draw stack
- **GameState Effects**:
  - `present`: Add 1 card, remove 1 selected card
  - `drawStack`: Remove 1 card from top, add 1 card to top
  - `activeStash`: Decrease by 1 candy
- **Next Abilities**: [1] - TAKE_CANDY
- **Decision Points**:
  - **Activation**: User chooses to activate ability
  - **Card Selection**: User selects which present card to return to draw stack

#### 17. CARDS_FROM_PAST
- **ID**: `cardsFromPast`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy)
- **Cards**: [8, 18, 30, 44] (4 cards)
- **Limit**: 1 use per turn
- **Description**: Move first 2 cards from past to present
- **GameState Effects**:
  - `present`: Add first 2 past cards
  - `past`: Remove first 2 cards
  - `activeStash`: Decrease by 1 candy
- **Next Abilities**: [2] - SCORE_CARD (for retrieved cards)
- **Decision Points**:
  - **Activation**: User chooses to activate ability

#### 18. CARD_INTO_FUTURE
- **ID**: `cardIntoFuture`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy)
- **Cards**: [12, 19, 32, 40] (4 cards)
- **Limit**: 1 use per turn
- **Description**: Move 1 present card to future area for next turn
- **GameState Effects**:
  - `present`: Remove 1 selected card
  - `futureAreas`: Add card to appropriate future area
  - `activeStash`: Decrease by 1 candy
- **Next Abilities**: [3] - (future resolution in next turn)
- **Decision Points**:
  - **Activation**: User chooses to activate ability
  - **Card Selection**: User selects which present card to move to future

#### 19. EXCHANGE_PRESENT_CARD_ORDER
- **ID**: `exchangePresentCardOrder`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: No
- **Cards**: None (general ability)
- **Limit**: 1 use per turn
- **Description**: Swap positions of any 2 cards in present area
- **GameState Effects**:
  - `present`: Reorder 2 selected cards
- **Next Abilities**: [3] - (continue with user actions)
- **Decision Points**:
  - **Activation**: User chooses to activate ability
  - **Card Selection**: User selects 2 cards to swap positions

#### 20. RESET_CANDIES
- **ID**: `resetCandies`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy)
- **Cards**: [37] (Card 37 only)
- **Limit**: 1 use per turn
- **Description**: Return all candy from present/future cards (except card 37) to reserve
- **GameState Effects**:
  - `reservedStash`: Increase by candy returned
  - Card candy counters: Reset to 0 (except card 37)
- **Next Abilities**: [3] - (continue with user actions)
- **Decision Points**:
  - **Activation**: User chooses to activate ability

#### 21. ALL_CARDS_INTO_FUTURE
- **ID**: `allCardsIntoFuture`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy)
- **Cards**: [16, 24, 26, 35, 38] (5 cards)
- **Limit**: 1 use per turn
- **Description**: Move all present cards to future areas, managing multiple future levels
- **GameState Effects**:
  - `present`: Clear all cards
  - `futureAreas`: Add new future area or shift existing ones
  - `activeAllCardsInFutureAreas`: Increment by 1
  - `activeStash`: Decrease by 1 candy
- **Next Abilities**: [4] - (affects future turn resolution)
- **Decision Points**:
  - **Activation**: User chooses to activate ability

#### 22. BELOW_THE_STACK
- **ID**: `belowTheStack`
- **Phase**: `USER_INPUT_REQUIRED`
- **Order**: 3
- **Candy Required**: Yes (1 candy)
- **Cards**: [4, 7, 29, 42] (4 cards)
- **Limit**: 1 use per turn
- **Description**: Place present cards under draw pile and end turn immediately
- **GameState Effects**:
  - `present`: Clear all cards
  - `drawStack`: Add cards to bottom
  - `activeStash`: Decrease by 1 candy
  - Turn state: Force turn end
- **Next Abilities**: [8] - END_TURN_END
- **Decision Points**:
  - **Activation**: User chooses to activate ability

## GameState Field Mapping

### Fields Affected by Abilities

| GameState Field | Affected by Abilities |
|---|---|
| `activeStash` | TAKE_CANDY (+), All manual abilities (-), SEQUENCE_RULE (+), RESET_CANDIES (+) |
| `reservedStash` | TAKE_CANDY (-), END_TURN_BEGIN (+), SEQUENCE_RULE (-), RESET_CANDIES (-) |
| `drawStack` | BEGIN_GAME (reset), BEGIN_TURN (-), SCORE_CARD (-), All draw abilities (-), EXCHANGE_CARD (±), END_TURN_END (+), BELOW_THE_STACK (+) |
| `present` | BEGIN_TURN (+), SCORE_CARD (±), END_TURN_BEGIN (-), All draw abilities (+), CARDS_INTO_PAST (±), EXCHANGE_CARD (±), CARDS_FROM_PAST (+), CARD_INTO_FUTURE (-), EXCHANGE_PRESENT_CARD_ORDER (reorder), ALL_CARDS_INTO_FUTURE (-), BELOW_THE_STACK (-) |
| `past` | END_TURN_BEGIN (+), DRINK_COFEE (trigger), END_TURN_END (-), CARDS_INTO_PAST (+), Draw abilities (source), CARDS_FROM_PAST (-) |
| `futureAreas` | BEGIN_TURN (resolve), CARD_INTO_FUTURE (+), ALL_CARDS_INTO_FUTURE (+) |
| `finishedPile` | SCORE_CARD (+), START (+) |
| `activeAllCardsInFutureAreas` | BEGIN_TURN (-), ALL_CARDS_INTO_FUTURE (+) |
| `gameEnd` | DRINK_COFEE (conditional), GAME_END_WIN (true), GAME_END_LOSE (true) |

## Decision Point Analysis

### DecisionProvider Requirements

Based on the ability analysis, the `DecisionProvider` interface must support:

#### Card Selection Methods
```java
// Single card selection from present area
Card chooseCardFromPresent(List<Card> availableCards, String prompt);

// Multiple card selection from present area
List<Card> chooseCardsFromPresent(List<Card> availableCards, int count, String prompt);

// Two card selection for swapping positions
List<Card> chooseTwoCardsToSwap(List<Card> presentCards, String prompt);

// Source selection when draw pile is empty
CardSource chooseCardSource(boolean drawAvailable, boolean pastAvailable, String prompt);
```

#### Activation Methods
```java
// Simple ability activation choice
boolean chooseToActivateAbility(AbilitySpec ability, int candyCost, String description);

// Multiple activation choice (for DRAW_ONE_3X)
int chooseActivationCount(int maxActivations, int candyCostEach, String description);
```

### Decision Matrix by Ability

| Ability | Decision Type | Options | Constraints |
|---|---|---|---|
| DRAW_TWO | Activation + Source | Activate? + Draw/Past source | 1 candy cost |
| CARDS_INTO_PAST | Activation + Card Selection | Activate? + 2 cards from present | 1 candy cost |
| DRAW_ONE | Activation + Source | Activate? + Draw/Past source | 1 candy cost |
| DRAW_ONE_3X | Activation Count + Source | 1-3 activations + Draw/Past per use | 1 candy per use |
| EXCHANGE_CARD | Activation + Card Selection | Activate? + 1 card to return | 1 candy cost |
| CARDS_FROM_PAST | Activation | Activate? | 1 candy cost |
| CARD_INTO_FUTURE | Activation + Card Selection | Activate? + 1 card to move | 1 candy cost |
| EXCHANGE_PRESENT_CARD_ORDER | Activation + Card Selection | Activate? + 2 cards to swap | No candy cost |
| RESET_CANDIES | Activation | Activate? | 1 candy cost |
| ALL_CARDS_INTO_FUTURE | Activation | Activate? | 1 candy cost |
| BELOW_THE_STACK | Activation | Activate? | 1 candy cost |

## Ability Execution Flow

### Automatic Flow (Per Turn)
```
1. BEGIN_TURN (order 0)
   ↓
2. TAKE_CANDY (order 1) - if applicable cards present
   ↓
3. SCORE_CARD (order 2) - if scoreable cards present
   ↓
4. USER_INPUT_REQUIRED phase - manual abilities (order 3)
   ↓
5. END_TURN_BEGIN (order 4)
   ↓
6. SEQUENCE_RULE (order 5)
   ↓
7. DRINK_COFEE (order 6) - if card 48 in past
   ↓
8. END_TURN_END (order 8)
   ↓
   Loop back to BEGIN_TURN or Game End
```

### Manual Ability Triggers
All manual abilities are available during `USER_INPUT_REQUIRED` phase when:
1. Associated card is present in present area
2. Player has sufficient candy (if required)
3. Ability hasn't been used yet this turn (respecting limits)

## Integration Requirements

### State Immutability
- All abilities must return new `GameState` instances
- No mutation of existing state objects
- Card objects with updated candy counters require new instances

### Event Publishing
- Each ability execution should publish `AbilityExecutedEvent`
- Events should include: ability type, affected state areas, decision inputs
- UI components subscribe to events for real-time updates

### Error Handling
- Validate candy availability before manual ability execution
- Check card availability for selection-based abilities
- Graceful handling of impossible states (empty draw + empty past)

### Performance Considerations
- Efficient state copying for large game states
- Lazy evaluation of ability availability
- Minimal UI updates through targeted event publishing

## Future Implementation Notes

1. **Decision UI Patterns**: Each decision type needs corresponding UI components
2. **Ability Validation**: Pre-execution validation prevents invalid game states
3. **Undo Mechanisms**: Consider ability rollback for accidental activations
4. **Save/Load**: State serialization must preserve all ability-related data
5. **AI Integration**: DecisionProvider interface allows AI player implementation

---

*This analysis provides the foundation for implementing the complete ability execution pipeline and UI decision workflows in Milestone 3.*
