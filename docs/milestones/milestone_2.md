# Milestone 2 — Card Component & Interactions (UI‑only)

This milestone builds on Milestone 1 by introducing the visual Card component and basic drag‑and‑drop interactions between the Active Stash (candy) and the Present Area cards. It is intentionally UI‑only: do not call into the engine/executors yet. The focus is on layout, assets, and intuitive DnD affordances.

## Goals
- Implement a reusable Card UI component that renders a single card using background images from the assets folder.
- Support two visual states for a card: normal and candy‑activated ("candy used").
- Provide drag‑and‑drop interactions:
  - Reorder cards within the Present by dragging one card over another (free action).
  - Drag a candy token from the Active Stash onto a Present card to mark it as candy‑activated (purely visual at this point).
- Bind all card sizes and spacing to `DimensionService`.

## Card assets and states
- Card front backgrounds are loaded from: `src/main/resources/assets/cards/`
  - Normal: `{card_number}_card.png` (e.g., `1_card.png`)
  - Candy‑activated: `{card_number}_cardc.png` (e.g., `2_cardc.png`)
- There are cards numbered from 1 to 48. Not all cards necessarily have a candy version, but when a `*_cardc.png` exists it represents the same card after its candy ability was activated.
- For Milestone 2 the Card component renders:
  - The appropriate background image (normal or candy‑activated).
  - No separate small ability icons or usage pips. The `*_cardc.png` variant itself is the only marker for candy activation in this milestone.

## Interactions
### 1) Reorder cards in Present
- Drag one Present card over another to swap their positions. This is a free action.
- IMPORTANT: Do not trigger any engine logic or auto‑scoring re‑evaluation on reorder in this milestone.
- Visual feedback: show a simple hover/highlight on the potential swap target.

### 2) Spend candy to activate a card (visual only)
- Drag a candy token from the Active Stash and drop it on a Present card.
- If the target card is one that “can accept candy” and is not already in candy‑activated state, change its background image from `{n}_card.png` to `{n}_cardc.png`.
- If the card is already candy‑activated, or not eligible for candy, the drop is considered invalid and should snap back (no state change).
- At this milestone:
  - Do NOT use `AbilityExecutor` (no engine integration, no state mutation in the model).
  - The Active Stash visual should reduce the filled candy count by one on a successful drop (as per Milestone 1 DnD affordances). On invalid drop, counts remain unchanged.

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
  - Renders a single row of Card components.
  - Supports card↔card swap via DnD.
  - Accepts candy drops from the Active Stash and toggles eligible cards to their `*_cardc.png` background.
- Future and Past Areas:
  - Display‑only (reuse the Card component for consistency).
- Active Stash Bar:
  - Candy tokens remain the drag source. On successful drop, decrement the filled count visually (increase outline). On failed drop, snap back with no change.

## Out of scope (deferred)
- No calls to `AbilityExecutor`, `AbilityExecutors`, or `DecisionProvider`.
- No actual game state mutation or rule validation beyond the UI‑only heuristic above.
- No animations beyond simple hover/highlight.

## Acceptance criteria
- Card component renders using `{n}_card.png` for normal and `{n}_cardc.png` for candy‑activated states.
- Dragging one Present card over another swaps their positions with clear visual feedback; no engine calls, no auto‑scoring re‑evaluation.
- Dragging a candy token onto an eligible Present card switches its background to the `*_cardc.png` image if it wasn’t already activated; otherwise the drop is rejected and visuals snap back.
- Active Stash visual updates: successful candy drop reduces filled tokens by one; failed drop leaves counts unchanged.
- All sizes and spacing respond to window resize via `DimensionService`.

## Notes for Milestone 3+
- Replace the UI‑only candy eligibility with engine‑driven validation using `game-abilities.json`.
- On activation, call the appropriate `AbilityExecutor` and update the immutable `GameState` snapshot.
- Reintroduce automatic scoring evaluation when the engine is wired, not in this milestone.
