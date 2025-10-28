# Finished! FX — Initial Implementation Requirements

This document lists the tasks for the first UI implementation of the Finished! solo card game. The game engine, model, and ability interfaces are already provided in:
- `com.adrian.finished.core` (executors for abilities/turn flow)
- `com.adrian.finished.model` (immutable game state and areas)
- `com.adrian.finished.model.abilities` (API: `AbilityExecutor`, `AbilityExecutors`, `AbilityContext`, `DecisionProvider`, etc.)

Reference docs and rules:
- `docs/core/architecture.md`, `docs/api/architecture.md`, `docs/core/runtime-executors.md`
- `src/main/resources/rule-files/game-info.md`, `src/main/resources/rule-files/game-loop.md`
- `src/main/resources/rule-files/game-abilities.json`

See Global UI rules in `docs/global_ui_rules.md`.

Milestone 1 — Core Views (Areas & Stashes) — UI layout
1. Stashes view
   - Render Active and Reserved stashes (coffee and candy counts). Provide drag-source for candy tokens from Active stash.
2. Present Area view
   - Responsive, single-row layout for cards. Implement auto-shrink logic to avoid overflow. Display-only at this milestone (prepare for future DnD). Optional selection highlight placeholder styles.
3. Future Areas view
   - Stack or column of future area rows. When they don’t fit vertically, shrink card scale or collapse oldest rows into a "+N" indicator while preserving FIFO semantics visually.
4. Past Area view
   - Show up to the last 3 past cards (as per rules). Include an indicator for older cards that will cycle under the draw stack at end turn.
5. Finished Pile view
   - Show scored cards progression and next required number.
6. DimensionService integration for area layouts
   - Each view (Stashes, Present, Future, Past, Finished) must consume a central `DimensionService` and recompute sizes on resize (no hardcoded px). Bind card scales, margins, and fonts to responsive units.

Milestone 2 — Card Component & Interactions (UI-only)
7. Card component
   - Renders the card front using background images from assets: `{n}_card.png` and `{n}_cardc.png` (candy-activated variant). Scales via `DimensionService`. No separate ability icons or pips at this milestone.
8. Drag-and-drop: reorder Present
   - Drag one card over another to swap positions (free action). Do not re-evaluate auto-scoring in this milestone.
9. Drag-and-drop: spend candy to activate (visual only)
   - Drag a candy token from Active stash onto a Present card. If `{n}_cardc.png` exists and the card isn’t already activated, switch the background to the `*_cardc.png` image and decrement the Active Stash visual by one; otherwise snap back with no change. Do not call `AbilityExecutor` yet.
10. DimensionService integration for card sizing and interactions
   - Bind card width/height and spacing to the `DimensionService`. The whole card acts as the drop target; no separate hotspots at this milestone.

Milestone 3 — Cross-Area Interactions & Basic Animations (UI-only)
11. Drag-and-drop: Present → Future
   - Drag a card from the Present Area into the Future Areas. On drop, snap the card into place at the end of the first Future row. Free action; UI-only (no engine calls).
12. Drag-and-drop: Present → Past
   - Drag a card from the Present Area into the Past Area. On drop, snap the card at the end of the Past list. UI-only (no engine calls).
13. Button: Move all Present to Future (UI-only)
   - A button that moves all cards from Present to the first Future row. Play a vertical translation animation for each card to the Future area. This is purely visual and does not invoke the engine.

Milestone 4 — Engine Wiring & App Shell
14. App bootstrap and full-screen shell
   - Create the JavaFX `Application` that starts full screen by default, handles dynamic resizing, and runs on desktop and JPro. Use a shared DimensionService (see next point) for layout. (No hardcoded px.)
15. DimensionService (responsive sizing)
   - Provide a central, observable service that computes sizes based on current scene width/height (e.g., base unit, card width/height, margins, font sizes). Update on resize.
16. Ability pipeline integration
   - Prepare an `AbilityExecutor` pipeline wrapped with `AbilityExecutors.withListeners(...)` to publish post-execution events for UI updates/animations.
   - Maintain the current `GameState` as an immutable snapshot; on each ability, replace with the returned state.
17. DecisionProvider adapter (UI-driven)
   - Implement a `DecisionProvider` that blocks via UI workflows to collect choices required by abilities (see methods below). Ensure it works on desktop and in JPro.

Milestone 5 — Turn Flow & Ability Decisions
18. Turn control bar
   - Buttons: New Game, Start Turn (Begin Turn), End Turn. Display current turn hints (e.g., future areas pending).
19. Automatic abilities handling
   - After state changes affecting Present/Draw (start turn or after an action), loop the automatic scoring sequence until no more scoring is possible (as per `game-loop.md`). Animate moves where feasible.
20. DecisionProvider UI flows
   - Implement the prompts required by `DecisionProvider`:
     - `selectPresentCardIndices(state, count)`: multi-select cards by index (with clear affordances and validation) and then continue executor.
     - `selectAbilityProviderCard(state, validCardNumbers)`: choose which card in Present provides the ability when multiple are eligible.
   - Ensure these prompts can be completed via mouse/touch and keyboard; avoid modal blocking where possible (use overlays that fulfill the synchronous API contract via CompletableFuture or similar).
21. Ability UX coverage
   - Provide UI flows for abilities described in `game-loop.md` and `game-abilities.json` (e.g., choose cards for past/future, exchange top of draw with present, below-the-stack confirmation that ends turn).

Milestone 6 — Feedback, Animations, and Edge Cases
22. Visual feedback and notifications
   - Indicate gains/spends of candy/coffee, auto-scoring events, and end-game outcomes. Non-intrusive to preserve flow.
23. Animations (lightweight)
   - Short transitions for moving cards between areas. Must degrade gracefully on JPro/mobile.
24. End-game states
   - Show win/lose overlay according to `GameEnd*Executor`. Provide action to start a new game.
25. Error handling
   - Surface invalid actions as gentle toasts/tooltips. Keep engine immutable/pure; guard UI side-effects.

Milestone 6 — Responsiveness, Accessibility, Quality
26. Responsiveness and scaling QA
   - Test layouts across common desktop resolutions and browser sizes. Verify no scrollbars appear; elements shrink appropriately.
27. Keyboard and accessibility
   - Keyboard navigation for core actions (focus ring, activate ability, reorder). Provide descriptive labels for screen readers where supported.
28. Theming and CSS
   - Central CSS that computes paddings/fonts via `em`/percentages. Keep color/contrast accessible.

Milestone 7 — JPro/Web Support
29. JPro environment detection
   - Use JPro WebAPI to adapt UI when running in browser (avoid native dialogs, adjust hover/drag affordances). Keep feature parity.

Deliverables & Definition of Done
- A playable vertical slice: New Game → Start Turn → reorder/activate at least `drawOne`, `drawTwo`, `exchangePresentCardOrder` → End Turn sequence with visible results and no scrollbars at typical resolutions.
- All sizes dynamic; present and future areas never cause scroll. Cards shrink as needed.
- DecisionProvider implemented and used by executors that require user input.
- Works both as JavaFX desktop and via JPro in the browser.
- README updated with overview and how to run.
