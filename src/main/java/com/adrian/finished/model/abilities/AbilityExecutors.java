package com.adrian.finished.model.abilities;

import com.adrian.finished.model.GameState;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Utility helpers for working with AbilityExecutor implementations.
 *
 * Keeps AbilityExecutor as a minimal, functional interface while offering
 * convenient decorators and factories here.
 */
public final class AbilityExecutors {
    private AbilityExecutors() { }

    /**
     * Wrap an existing AbilityExecutor so that, after each execution, all provided listeners are notified
     * with an immutable AbilityExecutedEvent. Useful to plug in UIs (Console, JavaFX) or loggers.
     *
     * Usage example:
     * <pre>
     * AbilityExecutionListener ui = event -> System.out.println("After: " + event.after());
     * AbilityExecutor base = ctx -> ctx.state(); // your implementation
     * AbilityExecutor exec = AbilityExecutors.withListeners(base, ui);
     * GameState newState = exec.apply(new AbilityContext(state, AbilitySpec.drawTwo));
     * </pre>
     */
    public static AbilityExecutor withListeners(AbilityExecutor delegate, AbilityExecutionListener... listeners) {
        if (delegate == null) throw new IllegalArgumentException("delegate cannot be null");
        return context -> {
            GameState before = context.state();
            GameState after = delegate.apply(context);
            AbilityExecutedEvent event = new AbilityExecutedEvent(context.ability(), before, after);
            notifySync(listeners == null ? List.of() : List.of(listeners), event);
            return after;
        };
    }

    /**
     * Convenience overload accepting a collection of listeners.
     */
    public static AbilityExecutor withListeners(AbilityExecutor delegate, Collection<AbilityExecutionListener> listeners) {
        if (delegate == null) throw new IllegalArgumentException("delegate cannot be null");
        final Collection<AbilityExecutionListener> safe = listeners == null ? List.of() : listeners;
        return context -> {
            GameState before = context.state();
            GameState after = delegate.apply(context);
            AbilityExecutedEvent event = new AbilityExecutedEvent(context.ability(), before, after);
            notifySync(safe, event);
            return after;
        };
    }

    /**
     * Wraps an executor and delivers the AbilityExecutedEvent to provided listeners asynchronously using the given Executor.
     * The returned executor still performs the state transition synchronously; only listener notifications are async.
     */
    public static AbilityExecutor withAsyncListeners(AbilityExecutor delegate, Executor executor, AbilityExecutionListener... listeners) {
        Objects.requireNonNull(delegate, "delegate cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");
        final List<AbilityExecutionListener> list = listeners == null ? List.of() : List.of(listeners);
        return context -> {
            GameState before = context.state();
            GameState after = delegate.apply(context);
            AbilityExecutedEvent event = new AbilityExecutedEvent(context.ability(), before, after);
            try {
                executor.execute(() -> notifySync(list, event));
            } catch (RuntimeException ignored) {
                // Swallow to avoid breaking game flow (e.g., RejectedExecutionException)
            }
            return after;
        };
    }

    /**
     * Convenience overload for async delivery accepting a collection of listeners.
     */
    public static AbilityExecutor withAsyncListeners(AbilityExecutor delegate, Executor executor, Collection<AbilityExecutionListener> listeners) {
        Objects.requireNonNull(delegate, "delegate cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");
        final Collection<AbilityExecutionListener> safe = listeners == null ? List.of() : listeners;
        return context -> {
            GameState before = context.state();
            GameState after = delegate.apply(context);
            AbilityExecutedEvent event = new AbilityExecutedEvent(context.ability(), before, after);
            try {
                executor.execute(() -> notifySync(safe, event));
            } catch (RuntimeException ignored) {
                // Swallow to avoid breaking game flow
            }
            return after;
        };
    }

    private static void notifySync(Collection<AbilityExecutionListener> listeners, AbilityExecutedEvent event) {
        if (listeners == null) return;
        for (AbilityExecutionListener listener : listeners) {
            if (listener == null) continue;
            try {
                listener.afterAbility(event);
            } catch (RuntimeException ignored) {
                // Listener exceptions are swallowed to not break game flow
            }
        }
    }
}
