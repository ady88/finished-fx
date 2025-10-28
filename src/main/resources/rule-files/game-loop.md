# Finished! — Game Loop (Step by Step)

This file describes the full turn-by-turn flow of the Finished! solo card game, based on the components, areas, and abilities defined in game-info.md and game-abilities.json.

## 0) One-time Setup (Game Start)
1. Set up tokens:
   - Place 7 coffee tokens and 5 candy tokens in the Active Stash.
   - Place the remaining 5 candy tokens in the Reserved Stash.
2. Build the Draw Stack:
   - Shuffle cards 1–47 (only once, now).
   - Place card 48 at the bottom of these shuffled cards.
   - The Draw Stack will never be shuffled again. You will cycle through it over several rounds while sorting cards in the Present Area.
3. Clear all play areas (Present, Past, Future Areas, Finished Pile). Begin the game.

Reference ability: automaticAbilities.beginGame

## 1) Turn Start
1. If you have one or more Future Areas pending from "All Cards Into Future" (tracked by activeAllCardsInFutureAreas > 0):
   - Move the first Future Area’s cards into the Present Area.
   - Decrease activeAllCardsInFutureAreas by 1.
2. Otherwise (no pending Future Areas):
   - Draw the top 3 cards from the Draw Stack and place them in the Present Area.
3. Take Candy (only if cards entered Present from the Draw Stack in step 2):
   - For each card that has the takeCandy symbol (cards: 3, 6, 10, 15, 21, 28, 36, 45), gain 1 candy by moving it from the Reserved Stash to the Active Stash.
   - Note: Do not gain candy for cards entering Present from Past or Future Areas.
4. Immediate Scoring:
   - If the next card needed on the Finished Pile (ascending from 1 upward) is present in the Present Area, automatically score it by moving it to the Finished Pile, then immediately draw a replacement card from the Draw Stack into the Present Area.
   - Repeat this check until no more cards can be auto-scored. Card 1 is scored immediately when drawn.

Reference abilities: automaticAbilities.beginTurn, automaticAbilities.takeCandy, automaticAbilities.scoreCard, automaticAbilities.start

## 2) Player Actions Phase (while it is still your turn)
You may now interact with the cards in the Present Area. You can reorder the Present Area and spend candy to trigger card abilities, observing each ability’s limits.

- Free action:
  - Exchange the order of any two cards in the Present Area (no candy required). Reference: manualAbilities.exchangePresentCardOrder.

- Candy actions (spend from Active Stash; return candy to cards as markers as needed):
  - drawTwo (card 2): Spend 1 candy to draw 2 additional cards from the Draw Stack. If the Draw Stack is empty, draw from the Past instead.
  - drawOne (cards 9, 14, 20, 27, 31, 34, 46): Spend 1 candy to draw 1 additional card (from Past if Draw Stack empty).
  - drawOne3x (card 47): Spend up to 3 candy; for each candy, draw 1 additional card (from Past if Draw Stack empty). Max 3 per turn for this card.
  - exchangeCard (cards 13, 22, 33, 39, 43): Spend 1 candy to draw the top card from the Draw Stack, then place any one Present card (including the newly drawn card) face down on top of the Draw Stack.
  - cardsIntoPast (cards 5, 11, 17, 23, 25, 41): Spend 1 candy to move exactly 2 Present cards to the Past, then draw 2 new cards from the Draw Stack into the Present.
  - cardsFromPast (cards 8, 18, 30, 44): Spend 1 candy to move the first 2 cards from the Past into the Present.
  - cardIntoFuture (cards 12, 19, 32, 40): Spend 1 candy to move one Present card into a Future Area. That card will be added to the Present at the start of your next turn (see Turn Start step above).
  - allCardsIntoFuture (cards 16, 24, 26, 35, 38): Spend 1 candy to move all Present cards into a Future Area.
    - If you use this again before resolving an existing Future Area, push the current Future Area one step further (creating a second Future Area) and place the new set of Present cards into the now-empty first Future Area.
    - Increase activeAllCardsInFutureAreas by 1 whenever this is activated. Future Areas resolve into Present at the start of upcoming turns, one per turn, first-in-first-out.
  - resetCandies (card 37): Spend 1 candy to remove all candy on cards in Present and Future Areas, except the candy on card 37. Return removed candy to the Reserved Stash so those abilities can be used again (if you have candy to spend).
  - belowTheStack (cards 4, 7, 29, 42): Spend 1 candy to place all Present cards under the Draw Stack in order and immediately end your turn (skip the usual move-to-Past step for those Present cards). Then apply the Turn End cleanup for the Past (see 3.3 below).

Notes:
- Each manual ability has its own per-turn limit as defined in game-abilities.json (most limit 1; drawOne3x limit 3). You may need to place candy on the specific card to show activation.
- After each action that changes the Present, re-check for immediate scoring and perform it as long as the next required number appears in Present (drawing a replacement each time). Continue until no further automatic scoring is possible.

## 3) End Your Turn
When you decide to end your actions (or an effect says you must end the turn), resolve the following in order:

3.1 Move Present to Past:
- Move all remaining cards from the Present Area to the Past Area.
- Remove any candy on those moved cards and return that candy to the Reserved Stash.

3.2 Sequence Rule (gain candy):
- In the cards you just moved to Past, find any ascending sequences of length 3 or more.
- For each such sequence, gain (sequence length − 1) candy from the Reserved Stash to your Active Stash.
- Multiple sequences may award candy independently in the same end step.

3.3 Coffee for 48 (and possible loss):
- If card 48 was moved into the Past Area, immediately spend 1 coffee token from the Active Stash.
- If this would bring coffee below 0, the game ends immediately with a loss.

3.4 Trim the Past:
- If the Past Area has more than 3 cards, move the oldest cards one by one to the bottom of the Draw Stack until only 3 cards remain in Past.
- If the Past has 3 or fewer cards, do nothing.

Reference abilities: automaticAbilities.endTurnBegin, automaticAbilities.sequenceRule, automaticAbilities.drinkCofee, automaticAbilities.endTurnEnd

## 4) Check End Conditions
- Victory: If you can score card 48 to the Finished Pile, the game ends immediately with a win. Reference: automaticAbilities.gameEndWin.
- Defeat: If card 48 enters Past and you cannot pay the 1 coffee (coffee would drop below 0), you immediately lose. Reference: automaticAbilities.gameEndLose.

## 5) Next Turn
- If you have not won or lost, begin the next turn and repeat from section 1) Turn Start.
- Remember: At Turn Start, if you have pending Future Areas (activeAllCardsInFutureAreas > 0), you first resolve the oldest Future Area into Present and decrement the counter; otherwise, draw 3 from the Draw Stack.

## Play Areas Reference (from game-info.md)
- Active Stash: Coffee and candy available to spend.
- Reserved Stash: Coffee and candy not currently available.
- Draw Stack: Source of new cards at start of turns and for draw effects.
- Present Area: Where you sort cards and use actions (spending candy) during your turn.
- Past Area: Where Present cards go when you end your turn; candy is removed from them. Only the first 3 most recent cards remain; older ones cycle to bottom of Draw Stack.
- Future Areas: One or more areas into which cards can be pushed; they resolve into Present at the start of later turns.
- Finished Pile: Scored cards in ascending order.

## Quick Turn Summary
1) Turn Start: Resolve Future into Present if any; else draw 3 to Present. Gain candy for takeCandy icons that entered from Draw. Auto-score any next-number cards, drawing replacements each time.
2) Player Actions: Reorder Present freely; spend candy to use abilities; after each change, repeat auto-scoring until none possible.
3) End Turn: Move Present to Past; return their candy; gain candy for ascending sequences; if 48 entered Past, spend 1 coffee or lose; then keep only 3 cards in Past by moving older ones under Draw.
4) Check End: Win if 48 is scored; lose if you cannot pay coffee when 48 enters Past.
5) Next Turn: Repeat.