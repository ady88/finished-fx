# Global UI rules (apply to all tasks)

- No hardcoded dimensions. All sizes must derive from the current screen or container size (responsive).
- No vertical or horizontal scrolling anywhere. When content would overflow, scale down elements to fit.
- Present Area: if many cards are drawn, shrink all card nodes proportionally to fit on one line without horizontal scroll.
- Future Areas: if too many would require vertical scroll, either shrink card nodes globally or omit rendering of the upper-most future areas with a compact indicator (e.g., "+2 more").
- Prefer drag-and-drop interactions over modal dialogs where practical.
- Keep the core packages unchanged. UI must integrate purely via `AbilityExecutor`, `AbilityExecutors`, and `DecisionProvider`.
