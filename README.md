# Finished FX

A JavaFX + JPro web/desktop application that implements the Finished! solo card game’s UI on top of the provided game engine.

## What is Finished FX?

Finished FX is the visual, interactive client for the Finished! card game. The game rules, engine, and models already exist in the core packages; this app focuses on user experience and controls while delegating all game logic to the engine.

- Engine and models live in:
  - `com.adrian.finished.core` — concrete executors for abilities and turn flow
  - `com.adrian.finished.model` — immutable game state and play areas
  - `com.adrian.finished.model.abilities` — ability APIs (`AbilityExecutor`, `AbilityContext`, `DecisionProvider`, helpers)
- Design/architecture references:
  - `docs/api/architecture.md`, `docs/core/architecture.md`, `docs/core/runtime-executors.md`
  - Rules: `src/main/resources/rule-files/game-info.md`, `src/main/resources/rule-files/game-loop.md`, `src/main/resources/rule-files/game-abilities.json`

This UI must adhere to these UX rules:
- No hardcoded dimensions. All sizes are computed dynamically relative to screen/container size.
- No scrolling (neither vertical nor horizontal). If content would overflow, elements (cards, areas) must shrink to fit.
- Present Area: always a single row; if many cards are present, they scale down uniformly to fit.
- Future Areas: if too many would require vertical scroll, shrink cards and/or collapse upper areas into a compact indicator (e.g., "+2 more").
- Prefer drag-and-drop interactions (e.g., reorder cards by dragging, drag a candy token onto a card to activate an ability). Provide accessible alternatives as needed.
- Do not modify the core engine/model packages; integrate through the public APIs (`AbilityExecutor`, `AbilityExecutors`, `DecisionProvider`).

See the detailed task list in `docs/requirements.md` for the initial implementation plan and deliverables.

## Running the application (Desktop)

Prerequisites:
- Java 24 or later
- Maven 3.6.0 or later

Run on desktop:
```
mvn javafx:run
```
The app starts full-screen and adapts layout to the current display. All dimensions are computed at runtime.

## Running in a Web Browser with JPro

This application can also run in a web browser using [JPro](https://www.jpro.one/), which enables JavaFX applications to run in the browser without plugins.

### Prerequisites for JPro

- Java 24 or later
- Maven 3.6.0 or later
- A modern web browser (Chrome, Firefox, Safari, Edge)

### Running with JPro Locally

To run the application in a web browser locally:

1. Start the JPro server:
   ```
   mvn jpro:run
   ```

2. Open your web browser and navigate to:
   ```
   http://localhost:8080/
   ```

The application will automatically detect whether it's running in a browser or as a desktop application and adjust its display accordingly.

### JPro Configuration

The JPro configuration is defined in the `pom.xml` file. The key components are:

1. JPro Repository:
   ```xml
   <repository>
       <id>jpro - sandec repository</id>
       <url>https://sandec.jfrog.io/artifactory/repo</url>
   </repository>
   ```

2. JPro WebAPI Dependency:
   ```xml
   <dependency>
       <groupId>com.sandec.jpro</groupId>
       <artifactId>jpro-webapi</artifactId>
       <version>${jpro.version}</version>
       <scope>compile</scope>
   </dependency>
   ```

The application uses the JPro WebAPI to detect the runtime environment and adapt its behavior accordingly. It checks for JPro-specific system properties to determine if it's running in a browser.

## Architecture at a glance

- UI layer (this module) renders areas (Active/Reserved Stash, Present, Past, Future, Finished) and forwards user actions to the engine.
- Game engine transformations are applied by `AbilityExecutor` implementations. State is immutable; each action returns a new `GameState`.
- Interactive abilities obtain choices via a UI-backed `DecisionProvider` implementation.

For an overview of executors and ability mappings see `docs/core/runtime-executors.md`. For the initial UI work plan see `docs/requirements.md`. 
