# More Decisions

## Else if
Players need somewhere to stand. We'll give `Player` a field `x` for its spot on a map that runs from 0 to 100:
```java
int x;
```
Number fields start at `0`, so every `Player` starts at the map's left edge.
Next, players should be able to move without walking off either edge. That means checking *two* things: did they go below 0, and did they go above 100?
```java
void move(int steps) {
	x = x + steps;
	if (x < 0) {
		x = 0;
	} else if (x > 100) {
		x = 100;
	}
}
```
`else if` means "otherwise, check *this* question instead." Java goes through the questions top to bottom and runs the code for the **first** one that's `true`. After that it skips the rest.

Here's `bob.move(-30)` with `bob` at 10:
1. `x = x + steps;` sets `x` to -20.
2. `x < 0` is `true`, so `x = 0;` runs.
3. That part ran, so Java skips the `else if` entirely.

And `bob.move(150)`, starting from 0:
1. `x = x + steps;` sets `x` to 150.
2. `x < 0` is `false`, so Java moves on to the next question.
3. `x > 100` is `true`, so `x = 100;` runs.

When none of the questions are `true` (say, `bob.move(5)` from 0), none of the code inside them runs, and `x` ends up at 5.
You can chain as many `else if`s as you like, and a plain `else` at the very end catches everything that didn't match.

Keeping a value between two limits comes up constantly in robot code. A motor's speed has to stay between -1 and 1, for example, and the code for that looks a lot like `move`.

{{#quiz ../quizzes/ch01-08-else-if.toml}}

## Combining questions
Some decisions need more than one question. Say a `Player` is "in danger" when they're alive *and* their health is below 20. `&&` joins the two questions:
```java
boolean isInDanger() {
	return isAlive() && health < 20;
}
```
`&&` means "and." It's `true` only when the questions on *both* sides are `true`. A `Player` with 10 health is in danger. A `Player` with 0 health isn't, because they're not alive anymore.

`||` means "or." It's `true` when *at least one* side is `true`. Here a `Player` is "at the edge" when they're at either end of the map:
```java
boolean isAtEdge() {
	return x == 0 || x == 100;
}
```
(To type `|`, hold Shift and press the backslash key, `\`, which is usually above Enter.)

| `a`     | `b`     | `a && b` | `a \|\| b` |
| ------- | ------- | -------- | ---------- |
| `true`  | `true`  | `true`   | `true`     |
| `true`  | `false` | `false`  | `true`     |
| `false` | `true`  | `false`  | `true`     |
| `false` | `false` | `false`  | `false`    |

The first row shows that `||` is also `true` when *both* sides are `true`. In everyday English, "or" sometimes means "one or the other, but not both." In Java it always means "at least one."

People coming from math often try `0 < x < 100` to say "x is between 0 and 100." Java won't accept that. You have to ask the two questions separately and join them with `&&`:
```java
boolean isInMiddle() {
	return x > 0 && x < 100;
}
```

## Not
The last piece is `!`, which means "not." It flips a `boolean`: `!true` is `false`, and `!false` is `true`.
We can use it to bring knocked-out players back. Only dead players should be able to respawn:
```java
void respawn() {
	if (!isAlive()) {
		health = maxHealth;
		x = 0;
	}
}
```
`!isAlive()` reads as "not alive." For a living `Player`, `isAlive()` is `true`, so `!isAlive()` is `false`, and nothing happens.
You've seen `!` before, in `!=` ("not equal to").

{{#quiz ../quizzes/ch01-08-and-or-not.toml}}

## Picking one of two values
Some if statements exist only to pick which value a variable gets:
```java
String status;
if (isAlive()) {
	status = "alive";
} else {
	status = "knocked out";
}
```
That's six lines for one small choice. Java has a shortcut:
```java
String status = isAlive() ? "alive" : "knocked out";
```
Read it as "is it alive? If so, `"alive"`. Otherwise `"knocked out"`." The question goes before the `?`, the value for `true` goes after it, and the value for `false` goes after the `:`. This is the **ternary operator** (it has three parts), and it does the same thing as the if statement above.
Save it for short, simple choices like this one. When either side gets long, or when you need to *do* something instead of picking a value, a normal if statement is easier to read.

{{#quiz ../quizzes/ch01-08-ternary.toml}}

## Putting it together
Here's the whole `Player` class now. Parts that haven't changed since the last lesson are hidden so the new code stands out. Click the eye button in the top-right corner of the code to see them. The copy button next to it always copies the *whole file*, hidden parts included.
From here on, every "Putting it together" shows each changed file with its name above it. Files that didn't change are in a collapsed section at the bottom called **Files that didn't change**. Each one also includes the answer to the previous lesson's exercise, so if yours didn't quite work, you can catch up by copying these files.
{{#listing ch01-08}}

## Try it yourself
Dead players can still walk around, and any player can cross the whole map in one move. Change `move` so that:
1. Dead players can't move at all.
2. Players can't move more than 10 steps at a time in either direction. If `steps` is more than that, the `Player` doesn't move.

*Hint: `&&` can join more than two questions, like `a && b && c`.*

To check your answer, change `move` in `Player.java` and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-08/Game.java}}
```
If every line prints what its comment says, your `move` works.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-08}}
`isAlive() && steps >= -10 && steps <= 10` joins three questions with `&&`, so the `Player` only moves when all three are `true`: they're alive, and `steps` is between -10 and 10.

If the checker printed what its comments said, your version works too, even if it looks different.

</details>
