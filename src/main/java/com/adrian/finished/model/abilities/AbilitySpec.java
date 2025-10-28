package com.adrian.finished.model.abilities;

import java.util.List;

/**
 * Enum of all abilities, with metadata sourced from rule-files/game-abilities.json.
 * This replaces the previous AbilitySpec interface and AbilityId record.
 */
public enum AbilitySpec {
    // Automatic abilities
    BEGIN_GAME(
            "beginGame",
            "Move all cards from finished pile, present,past and future areas into the draw deck. Shuffle all cards from 1 to 47 - basically all cards except card 48. The draw deck will contain shuffled cards 1 to 47 and the last card will be card 48. Cards will never be shuffled again and the player will cycle through them over several rounds - 8 total, number of candy minus one. Turn by turn the player may sort the cards in the present area.",
            "Draw deck should contain shuffled cards 1 to 47 and 48 below them.",
            false,
            List.of(),
            0,
            AbilityPhase.GAME_START,
            -1,
            List.of(0),
            1
    ),
    BEGIN_TURN(
            "beginTurn",
            "If 'activeAllCardsInFutureAreas' = 0 draw the first 3 cards from the draw stack and place them in the present area. If the activeAllCardsInFutureAreas > 0 place cards in the first available future area into the present area and decrease activeAllCardsInFutureAreas by 1.",
            "Draw 3 to present or resolve a future area into present.",
            false,
            List.of(),
            0,
            AbilityPhase.TURN_START,
            0,
            List.of(1),
            1
    ),
    TAKE_CANDY(
            "takeCandy",
            "Whenever a card with the takeCandy symbol enters the present area at the start of a turn, gain 1 candy token by moving it from the reserved stash into the active stash. Game starts with 5 candy in active stash and 5 candy in reserved stash. Note: the take candy ability only takes place if cards were retrieved in the present area from the draw stack not the past or future areas.",
            "Gain 1 candy when a takeCandy card enters present from draw.",
            false,
            List.of(3, 6, 10, 15, 21, 28, 36, 45),
            8,
            AbilityPhase.TURN_START,
            1,
            List.of(2),
            1
    ),
    SCORE_CARD(
            "scoreCard",
            "Place card in the sorted pile if it can be scored. Applies for cards 1 to 47. It can be scored only if it's the next higher up card in the sorted pile. So if the sorted pile contains 1, 2, 3 and in the present area you now draw 20, 6, 4 then you automatically place 4 in the sorted pile so now sorted contains 1, 2, 3 and 4. After this you must draw a new card from the draw pile",
            "If the next number, place it in sorted pile and draw a replacement.",
            false,
            List.of(),
            0,
            AbilityPhase.TURN_START,
            2,
            List.of(1, 9),
            1
    ),
    START(
            "start",
            "The starting card of the game. No special effect beyond scoring it automatically in the sorted pile when drawn in the present area.",
            "Score card 1 immediately when drawn.",
            false,
            List.of(1),
            1,
            AbilityPhase.TURN_START,
            2,
            List.of(1),
            1
    ),
    END_TURN_BEGIN(
            "endTurnBegin",
            "Move all cards from the present area into the past area. Remove the candy from all these moved cards putting this candy back in the reserved stash",
            "Move present cards to past and return their candies to reserve.",
            false,
            List.of(),
            0,
            AbilityPhase.TURN_END,
            4,
            List.of(5),
            1
    ),
    SEQUENCE_RULE(
            "sequenceRule",
            "If you move a sequence of at least 3 cards which are in ascending numerical order after doing the end turn begin ability you receive the number of candy of the sequence length minus one. You get this candy from the reserved stash and place them in the active stash. You may have multiple such sequences and you would get candy for each of them.",
            "Gain candy for ascending sequences of 3+ moved to past.",
            false,
            List.of(),
            0,
            AbilityPhase.TURN_END,
            5,
            List.of(6),
            1
    ),
    DRINK_COFEE(
            "drinkCofee",
            "If card 48 is moved into the past area, automatically spend 1 coffee token. If tokens would drop below 0, the game ends immediately.",
            "When 48 enters past, spend 1 coffee or lose immediately if below 0.",
            false,
            List.of(48),
            1,
            AbilityPhase.TURN_END,
            6,
            List.of(8, 10),
            1
    ),
    END_TURN_END(
            "endTurnEnd",
            "if there are more than 3 cards in the past area, place the oldest of these cards one after the other below the draw stack until 3 cards remain in the past area. If there are 3 or fewer cards in the past area then no cards are moved below the draw stack.",
            "Keep only 3 cards in past; put oldest under draw stack.",
            false,
            List.of(),
            0,
            AbilityPhase.TURN_END,
            8,
            List.of(0),
            1
    ),
    GAME_END_WIN(
            "gameEndWin",
            "Score card 48 - the game end with victory for the player.",
            "Game ended with victory for the player",
            false,
            List.of(),
            0,
            AbilityPhase.GAME_END_VICTORY,
            9,
            List.of(-1),
            1
    ),
    GAME_END_LOSE(
            "gameEndLose",
            "If card 48 is moved to the past and there are no more coffee tokens left player loses.",
            "Game ended with defeat for the player",
            false,
            List.of(),
            0,
            AbilityPhase.GAME_END_LOSE,
            10,
            List.of(-1),
            1
    ),

    // Manual abilities
    DRAW_TWO(
            "drawTwo",
            "Draw two additional cards from the draw pile for the cost of 1 candy.If there are no cards in the draw pile the card is retrieved from the past area instead.",
            "Spend 1 candy to draw two extra cards.",
            true,
            List.of(2),
            1,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(1),
            1
    ),
    CARDS_INTO_PAST(
            "cardsIntoPast",
            "Select 2 cards from the present area and move them into the past pile. Then draw 2 new cards from the draw stack into the present area",
            "Spend 1 candy to move 2 present cards to past and draw 2.",
            true,
            List.of(5, 11, 17, 23, 25, 41),
            6,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(1),
            1
    ),
    DRAW_ONE(
            "drawOne",
            "Draw one additional card from the draw pile. If there are no cards in the draw pile the card is retrieved from the past area instead.",
            "Spend 1 candy to draw 1 extra card.",
            true,
            List.of(9, 14, 20, 27, 31, 34, 46),
            8,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(1),
            1
    ),
    DRAW_ONE_3X(
            "drawOne3x",
            "Draw one additional card from the draw pile. Can be activated 3 times. So each time you put a candy on it you draw a card, can put maximum 3 candy on it.If there are no cards in the draw pile the card is retrieved from the past area instead.",
            "Spend up to 3 candy to draw up to 3 extra cards.",
            true,
            List.of(47),
            8,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(1),
            3
    ),
    EXCHANGE_CARD(
            "exchangeCard",
            "Draw one card from the draw stack and place it into the present area. Then take any one card from the present area - including the one you just drawn and place it face down on top of the draw stack",
            "Spend 1 candy to swap a present card with the top of draw stack.",
            true,
            List.of(13, 22, 33, 39, 43),
            5,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(1),
            1
    ),
    CARDS_FROM_PAST(
            "cardsFromPast",
            "Retrieve the first 2 cards from the past pile into the present area.",
            "Spend 1 candy to move first 2 past cards to present.",
            true,
            List.of(8, 18, 30, 44),
            4,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(2),
            1
    ),
    CARD_INTO_FUTURE(
            "cardIntoFuture",
            "Place one card from the present area into the future pile. When you end your turn by moving the cards from the present area to the past area and begin the next turn by drawing 3 new cards you add that card to the present area",
            "Spend 1 candy to move a present card to future for next turn.",
            true,
            List.of(12, 19, 32, 40),
            4,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(3),
            1
    ),
    EXCHANGE_PRESENT_CARD_ORDER(
            "exchangePresentCardOrder",
            "Exchange the order of any 2 cards from the present area.",
            "Swap the order of any two present cards.",
            false,
            List.of(),
            4,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(3),
            1
    ),
    RESET_CANDIES(
            "resetCandies",
            "Remove all candy currently placed on any cards in the present and future areas, except the one placed on card 37. Place these candy back on the reserved stash. Thus, you may activate all these actions again if you use more candy.",
            "Spend 1 candy to return all present/future candies except on card 37 to reserve.",
            true,
            List.of(37),
            1,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(3),
            1
    ),
    ALL_CARDS_INTO_FUTURE(
            "allCardsIntoFuture",
            "Move all cards from the present area into the future pile. On the next turn, those cards become the new present area. If when trying to activate the allCardsIntoFuture ability after you already activated once this action, you move the first future area one step further into a second future area and move the cards from the present area into the now empty first future area. You now draw 3 cards for the present area. after you decide to move the cards to the past area you rezolve the future areas in reverse order - first future area first. When this ability is executed keep track of the number of future areas by incrementing a 'activeAllCardsInFutureAreas' variable - by default 0.",
            "Spend 1 candy to move all present cards to future areas.",
            true,
            List.of(16, 24, 26, 35, 38),
            5,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(4),
            1
    ),
    BELOW_THE_STACK(
            "belowTheStack",
            "Place all cards from the present area under the draw pile and immediately end the current turn. With this you skip the move to the past step since the present area cards will go in order below the draw stack. After doing this follow the moveToPast ability rule - if there are more than 3 cards in the past area, place the oldest of these cards one after the other below the draw stack until 3 cards remain in the past area. If there are 3 or fewer cards in the past area then no cards are moved below the draw stack.",
            "Spend 1 candy to place present under draw pile and end turn.",
            true,
            List.of(4, 7, 29, 42),
            4,
            AbilityPhase.USER_INPUT_REQUIRED,
            3,
            List.of(8),
            1
    );

    private final String id;
    private final String detailedDescription;
    private final String shortDescription;
    private final boolean requiresCandy;
    private final List<Integer> cards;
    private final int frequency;
    private final AbilityPhase phase;
    private final int order;
    private final List<Integer> next;
    private final int limit;

    AbilitySpec(String id,
                String detailedDescription,
                String shortDescription,
                boolean requiresCandy,
                List<Integer> cards,
                int frequency,
                AbilityPhase phase,
                int order,
                List<Integer> next,
                int limit) {
        this.id = id;
        this.detailedDescription = detailedDescription;
        this.shortDescription = shortDescription;
        this.requiresCandy = requiresCandy;
        this.cards = List.copyOf(cards);
        this.frequency = frequency;
        this.phase = phase;
        this.order = order;
        this.next = List.copyOf(next);
        this.limit = limit;
    }

    public String id() { return id; }
    public String detailedDescription() { return detailedDescription; }
    public String shortDescription() { return shortDescription; }
    public boolean requiresCandy() { return requiresCandy; }
    public List<Integer> cards() { return cards; }
    public int frequency() { return frequency; }
    public AbilityPhase phase() { return phase; }
    public int order() { return order; }
    public List<Integer> next() { return next; }
    public int limit() { return limit; }
}