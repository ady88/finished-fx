package com.adrian.finished.core;

import com.adrian.finished.model.Card;
import com.adrian.finished.model.DrawStack;
import com.adrian.finished.model.PastArea;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Small shared helper for draw-style abilities to fetch up to N cards
 * from the draw stack, falling back to past if needed.
 */
final class DrawHelpers {
    private DrawHelpers() {}

    /**
     * Draws up to count cards, preferring draw stack, then past (newest first from past).
     * Cards from draw stack keep their fromDrawStack flag (usually true); from past they are flagged false.
     */
    static Result drawUpTo(int count, DrawStack drawStack, PastArea past) {
        Deque<Card> ds = new ArrayDeque<>(drawStack.cards());
        Deque<Card> pastDeque = new ArrayDeque<>(past.cards());
        List<Card> drawn = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            if (!ds.isEmpty()) {
                Card c = ds.removeFirst();
                drawn.add(c); // keep original fromDrawStack flag (should be true for deck-created cards)
            } else if (!pastDeque.isEmpty()) {
                Card c = pastDeque.removeLast(); // newest from past
                // mark as fromDrawStack = false when retrieved from past
                drawn.add(new Card(c.number(), c.abilitiesTriggered(), c.maxAbilities(), false));
            } else {
                break; // no more sources
            }
        }
        return new Result(new DrawStack(ds), new PastArea(pastDeque), drawn);
    }

    static class Result {
        final DrawStack newDraw;
        final PastArea newPast;
        final List<Card> drawn;
        Result(DrawStack newDraw, PastArea newPast, List<Card> drawn) {
            this.newDraw = newDraw;
            this.newPast = newPast;
            this.drawn = drawn;
        }
    }
}
