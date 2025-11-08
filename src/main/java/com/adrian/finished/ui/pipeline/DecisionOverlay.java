package com.adrian.finished.ui.pipeline;

import javafx.scene.layout.StackPane;
import java.util.function.Consumer;

/**
 * Base interface for decision overlay components that collect user input
 * for ability execution. Each overlay shows UI elements for a specific
 * type of decision and blocks until the user provides input.
 *
 * @param <T> The type of result this overlay produces
 */
public abstract class DecisionOverlay<T> extends StackPane {

    protected volatile T result;
    protected volatile boolean decisionMade = false;
    protected final Object lock = new Object();

    // Add callback support for async operation
    private Consumer<T> onDecisionCompleteCallback;

    /**
     * Blocks the calling thread until the user makes a decision.
     * @return The user's decision result, or null if cancelled
     */
    public T waitForDecision() {
        synchronized (lock) {
            while (!decisionMade) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            return result;
        }
    }

    /**
     * Set a callback to be notified when decision is complete (for async operation).
     */
    public void setOnDecisionComplete(Consumer<T> callback) {
        this.onDecisionCompleteCallback = callback;
    }

    /**
     * Called by UI handlers when a decision is made.
     * Wakes up any threads waiting for the decision.
     */
    protected void completeDecision(T result) {
        synchronized (lock) {
            this.result = result;
            this.decisionMade = true;
            lock.notifyAll();
        }

        // Notify async callback if set
        if (onDecisionCompleteCallback != null) {
            onDecisionCompleteCallback.accept(result);
        }
    }

    /**
     * Called when the user cancels the decision.
     * Results in a null return value.
     */
    protected void cancelDecision() {
        completeDecision(null);
    }
}