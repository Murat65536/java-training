# Project: A Tournament

Until now, every lesson has told you what to write. This one doesn't. It's a project: you'll add something bigger to the game on your own, using everything from this part of the book. There's no answer at the end, since there's no single right answer. When you finish, show a mentor or teammate and walk them through how it works.

## What to build
Make a `Tournament` class that has a group of fighters battle until only one is left standing.

**The tournament**
- A `Tournament` has a list of fighters and a way to add a `Player` to it.
- It has a method that runs the whole tournament. The tournament is played in **rounds**. In each round, every fighter who isn't knocked out gets one turn, in the order they were added.
- On their turn, a fighter attacks the weakest *other* fighter who isn't knocked out. (Your `attackWeakest` from [Lists](ch01-19-lists.md) will help.)
- The tournament ends when only one fighter is left or after `MAX_ROUNDS` rounds, whichever comes first. Put `MAX_ROUNDS` in `Constants`.

**Printing what happens**
- At the end of each round, print the round number and every fighter's health as a percentage.
- At the end, print the winner. If more than one fighter is still standing after `MAX_ROUNDS`, print that it's a draw and who's left.

**Trying it out**
- In `Game`'s `main`, set up a tournament with at least four fighters: a regular `Player`, a `Tank`, a `Healer` and a `Berserker`. Give some of them weapons or stances, then run it.

## Hints
Try each part yourself first, and open a hint only when you're stuck.

<details>
<summary>How do I tell fighters apart when I print them?</summary>

Give `Player` a name. A constructor that takes a name and a starting health would work, but then `Tank`, `Healer` and `Berserker` would all need changes too. Another option is to keep the old constructor and add a second one, using [overloading](ch01-09-objects-working-together.md#same-name-different-parameters). [`toString`](ch01-12-protecting-your-fields.md#printing-an-object) will help with the printing.

</details>

<details>
<summary>How do I count how many fighters are still standing?</summary>

Write a small `private` method in `Tournament` that loops through the list with a `for` loop and counts the fighters who are alive. The tournament's main loop can keep going while that count is more than 1.

</details>

<details>
<summary>Java won't let me hand my list of fighters to `attackWeakest`</summary>

`attackWeakest` takes an `ArrayList<Damageable>`, and your list is probably an `ArrayList<Player>`. Every `Player` is a `Damageable`, but Java still won't let one kind of list stand in for the other. Instead, on each fighter's turn, make a new `ArrayList<Damageable>` and add every *other* fighter to it. That also takes care of the "other" part, since a fighter shouldn't attack themselves.

</details>

<details>
<summary>Which kind of loop should I use for the rounds?</summary>

You don't know in advance how many rounds there will be, but you do know the maximum. A `while` loop that checks both works well: keep going while more than one fighter is standing *and* the round number is at most `MAX_ROUNDS`.

</details>

## Going further
If you finish early, try some of these:
- Make stances matter. Before each turn, a fighter below half health switches to `DEFENDING`, and one above three quarters switches to `AGGRESSIVE`.
- `Player` already counts knockouts in its score. Print a leaderboard at the end, from highest score to lowest.
- Let fighters with an ability use it instead of attacking, once every few rounds.
- Let a `Healer` sometimes heal the weakest fighter on its turn instead of attacking. How do you decide when?
- Add `Crate`s to the tournament as targets. What has to change once not everything in the tournament is a `Player`?
