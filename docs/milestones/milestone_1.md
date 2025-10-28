# Milestone 1 — Core Views (Areas & Stashes) — UI layout

This milestone delivers the foundational, responsive UI layout for the Finished! game without wiring full gameplay logic. It establishes the screen structure, areas, and visual affordances required by later milestones. No scrolling is allowed; all dimensions are dynamic and tied to the current window size.

## Goals
- Establish a stack-based root layout with a background layer and content layers.
- Split the screen vertically into four areas with specified proportions (Future, Present, Past, Active Stash bar).
- Add an overlay element for Finished Pile, positioned above the four areas (no Draw Deck overlay in this milestone).
- Implement a responsive `DimensionService` used by all views to compute sizes and update on resize.
- Provide the Active Stash bar visuals and drag-source behavior for candy tokens (visual + DnD affordance only; actual engine integration comes later).

## Root structure and layers
- Root container: Stack layout (JavaFX `StackPane`).
  - Layer 1 (bottom): Background node (Rectangle/Pane) with a light color between green–yellow–gray (e.g., soft lime/olive tint). Can be themed via CSS.
  - Layer 2 (middle): The 4 vertically stacked areas in a single container (e.g., VBox or custom pane).
  - Layer 3 (top overlay): Finished Pile visual component (anchored bottom-left) that sits above Layer 2. No Draw Deck overlay in this milestone.

Notes:
- Even if the Layer 2 content visually covers the background entirely, the background remains in place for a consistent theme and quick theming changes.
- All sizes are computed via `DimensionService`. No fixed pixel values.

## Vertical split (Layer 2)
Split the screen into 4 parts (percentages of full height):
- Future Areas view: 30%
- Present Area view: 40%
- Past Area view: 24%
- Active Stash bar: 6%

Implementation guidance:
- Use `VBox` with four child containers. Bind each child’s `prefHeight` to `DimensionService` outputs derived from scene height.
- Ensure no vertical scrollbars. If content doesn’t fit, scale/shrink contents within each area according to rules below.

### Future Areas view (30%)
- Layout: Column of future rows (top to bottom = oldest to newest, or visually FIFO-consistent).
- Overflow handling: If rows wouldn’t fit, shrink card scale uniformly. If still insufficient, collapse the oldest rows into a compact indicator (e.g., “+N”).
- Dimensioning: Card sizes and spacing come from `DimensionService`.
- Interactivity at this milestone: display-only (DnD/selection comes later).

### Present Area view (40%)
- Layout: Single row of cards, left-to-right.
- Overflow handling: auto-shrink cards uniformly to keep a single row without horizontal scrolling.
- Visuals: optional selection highlighting placeholder styles for later interactions.
- Interactivity at this milestone: display-only. Prepare drop target overlays for future candy drag (hit zones sized by `DimensionService`).

### Past Area view (24%)
- Layout: Show up to the latest 3 past cards; indicate any older cards compactly (e.g., “+N older to Draw”).
- Interactivity: none (no user interaction is needed in Past).

### Active Stash bar (6%)
- Horizontal bar holding, from left to right:
  1) Candy tokens (up to 10),
  2) Coffee token icon + numeric count,
  3) End Turn button.
- Spacing: elements placed horizontally with consistent gaps, all sizes from `DimensionService`.
- Candy tokens:
  - The Active Stash can contain at most 10 candy tokens.
  - Render 10 slots horizontally:
    - Filled token for each candy currently available in the Active Stash.
    - Outline (ghost) token for empty slots.
  - Assets:
    - Filled candy image: `src/main/resources/assets/other/candy.png`
    - Outline candy image: `src/main/resources/assets/other/candy-outline.png`
  - Drag-and-drop source behavior (visual layer only in this milestone):
    - When the player drags one candy and successfully drops it onto a card with an open (valid, candy-requiring) ability, the Active Stash visual updates to show one fewer filled token and one outline in its place. Example: starting with 10 filled tokens, after a successful drop, show 9 filled + 1 outline.
    - If the drag operation is not successful (drop on invalid target or cancelled), snap the token back to its original slot; no change in counts.
    - Constraints/validation of “open ability” and state changes will be implemented when wiring the engine in later milestones; for now, provide the DnD affordances and success/failure visual pathways that can be triggered by mock callbacks.
- Coffee display: Icon + numeric label (e.g., “x7”). Size and font scale from `DimensionService`. Use coffee token image from `src/main/resources/assets/other/coffee.png`. 
- End Turn button: Right-aligned element within the bar. Emits a UI event in later milestones; for now, render the button and ensure it sizes/aligns correctly.

## Overlay elements (Layer 3)
- Finished Pile: anchored bottom-left, on top of all other elements (above Layer 2). Vertically positioned 6% above the absolute bottom to avoid interfering with Active Stash interactions. Non-interactive at this milestone.

Sizing and positioning rules:
- Finished Pile component has:
  - Width: 10% of the screen width.
  - Height: 20% of the screen height.
- Positioning:
  - Finished Pile: bottom-left corner, with its bottom aligned at 6% above the overall screen bottom (i.e., bottom offset equals the Active Stash bar height) so it visually floats above the stash bar.
- Use a simple layout helper or bindings in `DimensionService` to compute this rectangle and anchor it.

## DimensionService (responsive sizing)
Provide a central, observable service to compute sizes and notify views on resize. Examples of exposed values/bindings:
- `sceneW`, `sceneH` (read-only properties)
- Area heights: `futureH = 0.30 * sceneH`, `presentH = 0.40 * sceneH`, `pastH = 0.24 * sceneH`, `stashH = 0.06 * sceneH`
- Card base dimensions: `cardH` derived from the area height, `cardW` from aspect ratio (consistent across areas)
- Token sizes: `candySize`, `coffeeIconSize`
- Typography: `baseFont`, `smallFont`
- Margins/gaps: `gapSmall`, `gapMedium`, `gapLarge` as fractions of `sceneW`/`sceneH`
- Overlay sizes: `pileW = 0.10 * sceneW`, `pileH = 0.20 * sceneH`
- Overlay positions: `finishedBottom = stashH + gapMedium`

All top-level views (Future/Present/Past/ActiveStash, FinishedPile) must read from this service and update when it changes.

## Visual and UX rules carried into this milestone
- No scrolling (neither vertical nor horizontal). If content would overflow, shrink content within its allotted area.
- All dimensions and font sizes are computed; no hardcoded pixel values.
- Maintain clear spacing and alignment at common desktop resolutions and within a browser via JPro.

## Acceptance criteria
- The root uses a stack layout with:
  - a themed background layer,
  - a four-area vertical layout layer (30/40/24/6 height split),
  - an overlay element for the Finished Pile (10% width, 20% height) vertically offset by 6% of screen height from the absolute bottom to avoid interfering with the Active Stash interactions. No Draw Deck overlay in this milestone.
- Active Stash bar shows up to 10 candy tokens in horizontal order with appropriate spacing, followed by a coffee icon/label and an End Turn button.
- Candy DnD affordance exists visually (token can be dragged). On a simulated successful drop, the bar updates to show one fewer filled token and one outline; on failed drop, it snaps back and counts remain unchanged.
- Present and Future areas do not introduce scrollbars; when space is constrained, cards scale down uniformly within their areas.
- All sizes update correctly on window resize (verified by observing `DimensionService`-bound changes).

## Open questions / notes for later milestones
- Exact visuals for candy outline/filled states (assets, CSS) to be finalized.
- Animations for token drag, card scaling, and area transitions will be added in the animations milestone.
- Engine integration: Real validation of “open ability” targets and token/counter updates will be connected via `AbilityExecutor` and a UI-backed `DecisionProvider` in later milestones.
- Accessibility labels and keyboard navigation patterns are deferred to the responsiveness/accessibility milestone.
