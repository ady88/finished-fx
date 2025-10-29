# Milestone 2 — Card Component & Interactions (UI‑only)

## Card Model and UI Class

- The card model is defined in [`src/main/java/com/adrian/finished/model/Card.java`]. This immutable class represents a single card and its state.
- For this milestone, implement a corresponding Card UI class that visually represents a card, binds to its model, and supports state changes (normal/candy-activated).
- The Card UI class should use the model's `number` field to select the correct background image, and visually reflect candy activation.
- **Card UI must support two size variants:**
  - **Normal:** Used in Present area, supports interactions.
  - **Small:** Used in Future areas, Past area, and Finished pile (see below).

## Goals
- Implement a reusable Card UI component that renders a single card using background images from the assets folder.
- Support two visual states for a card: normal and candy‑activated ("candy used").
- Provide interactions:
  - Reorder cards within the Present by dragging one card over another (free action).
  - **Activate a card by clicking:** The user clicks a Present card to attempt to place a candy on it (see below).
- Bind all card sizes and spacing to `DimensionService`.

## Card assets and states
- Card front backgrounds are loaded from: `src/main/resources/assets/cards/`
  - Normal: `{card_number}_card.png` (e.g., `1_card.png`)
  - Candy‑activated: `{card_number}_cardc.png` (e.g., `2_cardc.png`)
  - **Small variant:** `{card_number}_card_small.png` (e.g., `1_card_small.png`)
    - Used in Future areas, Past area, and Finished pile.
- There are cards numbered from 1 to 48. Not all cards necessarily have a candy version, but when a `*_cardc.png` exists it represents the same card after its candy ability was activated.
- For Milestone 2 the Card component renders:
  - The appropriate background image (normal or candy‑activated).
  - No separate small ability icons or usage pips. The `*_cardc.png` variant itself is the only marker for candy activation in this milestone.
  - **Small variant images are always used in Future areas, Past area, and Finished pile due to limited space and lack of interaction.**

## Interactions
### 1) Reorder cards in Present
- Drag one Present card over another to swap their positions. This is a free action.
- IMPORTANT: Do not trigger any engine logic or auto‑scoring re‑evaluation on reorder in this milestone.
- Visual feedback: show a simple hover/highlight on the potential swap target.

### 2) Spend candy to activate a card (visual only)
- **No drag action required.**
- The user simply clicks on a Present card to attempt to activate it with a candy.
- If the target card “can accept candy” and is not already in candy‑activated state, change its background image from `{n}_card.png` to `{n}_cardc.png`.
- If the card is already candy‑activated, or not eligible for candy, the click is ignored (no state change).
- At this milestone:
  - Do NOT use `AbilityExecutor` (no engine integration, no state mutation in the model).
  - The Active Stash visual should reduce the filled candy count by one on a successful activation. On invalid click, counts remain unchanged.

### Determining “can accept candy”
- UI‑only heuristic for Milestone 2:
  - If an image `{n}_cardc.png` exists for card `n`, treat that card as candy‑eligible; otherwise treat as ineligible.
  - A card that is already showing its `*_cardc.png` variant is considered “already used”.
- This simple rule will be replaced by real validation in later milestones when wiring the engine and `game-abilities.json`.

## DimensionService bindings
- Bind all card/ui sizes to `DimensionService` (no hardcoded pixels):
  - Card width/height derived from the Present/Future/Past area heights.
  - Spacing/margins and font sizes derived from responsive units in the service.
- Interactable hotspots: there are no separate hotspots for this milestone. The whole card node is the drag target/drop zone. Resize should keep the entire card as the active target.

## View responsibilities
- Present Area:
  - Renders a single row of Card components (normal size).
  - Supports card↔card swap via DnD.
  - **Supports candy activation by clicking a card.**
- Future and Past Areas:
  - Display‑only (reuse the Card component for consistency).
  - **Always use small card variant images.**
- Finished Pile:
  - Holds at most one card (the latest sorted card).
  - **Always use small card variant image.**
- Active Stash Bar:
  - **Candy tokens are not draggable in this milestone.**
  - When the user clicks a Present card and successfully activates it with a candy, decrement the filled candy count visually (increase outline).
  - On invalid activation (click ignored), counts remain unchanged.

## Mock Data Task

- As a separate step, mock some data to test the UI layout:
  - Place a test card in each area: Present, Past, Future, and Finished pile.
  - Use the appropriate card variant for each area (normal for Present, small for others).
  - Finished pile should display at most one card (the latest sorted card).

## Out of scope (deferred)
- No calls to `AbilityExecutor`, `AbilityExecutors`, or `DecisionProvider`.
- No actual game state mutation or rule validation beyond the UI‑only heuristic above.
- No animations beyond simple hover/highlight.

## Acceptance criteria
- Card component renders using `{n}_card.png` for normal and `{n}_cardc.png` for candy‑activated states.
- Dragging one Present card over another swaps their positions with clear visual feedback; no engine calls, no auto‑scoring re‑evaluation.
- **Clicking an eligible Present card switches its background to the `*_cardc.png` image if it wasn’t already activated; otherwise the click is ignored and visuals remain unchanged.**
- Active Stash visual updates: successful candy activation reduces filled tokens by one; failed activation leaves counts unchanged.
- All sizes and spacing respond to window resize via `DimensionService`.

## Notes for Milestone 3+
- Replace the UI‑only candy eligibility with engine‑driven validation using `game-abilities.json`.
- On activation, call the appropriate `AbilityExecutor` and update the immutable `GameState` snapshot.
- Reintroduce automatic scoring evaluation when the engine is wired, not in this milestone.
