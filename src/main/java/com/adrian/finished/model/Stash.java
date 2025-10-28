package com.adrian.finished.model;

/**
 * Represents a stash of tokens (candy and coffee). Immutable.
 */
public record  Stash(int candy, int coffee) {
    public Stash {
        if (candy < 0) throw new IllegalArgumentException("candy cannot be negative");
        if (coffee < 0) throw new IllegalArgumentException("coffee cannot be negative");
    }
}