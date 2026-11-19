# If Statements

[Methods](ch01-06-methods.md) ended with a problem:
```java
Player bob = new Player(100);
bob.takeDamage(500);
```
That leaves `bob` at -400 health. We want `takeDamage` to say "if health went below 0, set it back to 0."
To write that, code first needs a way to *ask questions*.

## Asking questions
[Variables](ch01-02-variables.md) introduced `boolean`, a type that holds `true` or `false` and nothing else. It hasn't been useful so far. Now it is:
```java
boolean belowZero = health < 0;
```
`health < 0` asks "is `health` less than 0?" The answer is `true` or `false`, so it's a `boolean`. If `health` is -400, `belowZero` is `true`. If `health` is 70, `belowZero` is `false`.
These are all the questions you can ask about numbers:

| `<`       | `>`          | `<=`                  | `>=`                     | `==`     | `!=`         |
| --------- | ------------ | --------------------- | ------------------------ | -------- | ------------ |
| Less than | Greater than | Less than or equal to | Greater than or equal to | Equal to | Not equal to |

"Equal to" is `==`, with *two* equals signs, because a single `=` already means "set," as in [Methods](ch01-06-methods.md).
- `health = 0` means "set `health` to 0."
- `health == 0` means "is `health` 0?"

The two are easy to mix up, so watch for it.

{{#quiz ../quizzes/ch01-07-if-questions.toml}}

## Making decisions
Once code can ask a question, it can use the answer to decide whether to run something. That's what an **if statement** does:
```java
void takeDamage(int amount) {
	health = health - amount;
	if (health < 0) {
		health = 0;
	}
}
```
The parts:

| `if`                       | `(health < 0)`                               | `{` ... `}`                             |
| -------------------------- | -------------------------------------------- | --------------------------------------- |
| "Only do this if..." | The question (it must be `true` or `false`) | The code that runs only if it's `true` |

Here's `bob.takeDamage(500)`, starting from 100 health:
1. `health = health - amount;` sets `health` to -400.
2. `health < 0` is `true`, so the code inside the `{` `}` runs.
3. `health = 0;` sets `health` to 0.

And `bob.takeDamage(30)`:
1. `health = health - amount;` sets `health` to 70.
2. `health < 0` is `false`, so Java skips everything inside the `{` `}`.

In both cases `bob` stays at 0 or above.

The question in the `()` *has* to be a `boolean`. `if (health) { }` gets a red squiggly line, because `health` is a number, and a number isn't a question.

{{#quiz ../quizzes/ch01-07-if-decisions.toml}}

## Otherwise...
To do one thing when the answer is `true` and something else when it's `false`, add an `else`:
```java
boolean isAlive() {
	if (health > 0) {
		return true;
	} else {
		return false;
	}
}
```
When `health > 0` is `true`, the first `{` `}` runs. When it's `false`, the `else` `{` `}` runs. One of them always runs, and never both.

Look closely, though. When `health > 0` is `true`, the method returns `true`, and when it's `false`, it returns `false`. It's handing back the answer to the question. `health > 0` is already a `boolean`, so the method can return it directly:
```java
boolean isAlive() {
	return health > 0;
}
```
Both versions do the same thing. You'll usually see the shorter one.

## Where variables live
Variables don't last forever. A variable exists from the line where it's made down to the `}` that closes the `{` `}` it was made in. That stretch of code is its **scope**.
```java
int health = 30;
if (health < 50) {
	int bonus = 20;
	health = health + bonus;
}
System.out.println(health);
System.out.println(bonus);
```
The last line gets a red squiggly line saying `bonus cannot be resolved to a variable`. `bonus` was made inside the if statement's `{` `}`, so it's gone after the `}`. `health` was made outside, so it's still there.
If you need a variable after the `}`, make it before the `{`:
```java
int health = 30;
int bonus = 0;
if (health < 50) {
	bonus = 20;
	health = health + bonus;
}
System.out.println(bonus);
```
Methods follow the same rule. A variable made inside a method, or a parameter like `amount`, exists only while the method runs. That's why two methods can each have their own `amount` without mixing them up. Fields work differently. They're made outside every method, so they last as long as the object does, and any method in the class can use them.

{{#quiz ../quizzes/ch01-07-scope.toml}}

## Putting it together
The new `isAlive` method can be the question in an if statement. Here's a `heal` method that only works on living players:
```java
void heal(int amount) {
	if (isAlive()) {
		health = health + amount;
	}
}
```
It says `isAlive()`, not `bob.isAlive()`. Inside a class, a method can call the class's other methods directly, the same way it uses `health` without saying whose. It always means the `Player` that `heal` was called on.

The whole `Player` class so far:
{{#listing ch01-07}}

{{#quiz ../quizzes/ch01-07-if-else.toml}}

## Try it yourself
Nothing stops this right now:
```java
Player bob = new Player(100);
bob.heal(1000);
```
`bob` ends up with 1100 health even though `maxHealth` is 100. Change `heal` so `health` can never go above `maxHealth`.
*Hint: look at how `takeDamage` keeps `health` from going below 0.*

To check your answer, change `heal` in `Player.java` and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-07/Game.java}}
```
If every line prints what its comment says, your `heal` works.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-07}}
Only the part you needed to change is showing. To see where it fits in the whole class, click the eye button in the top-right corner of the code.

There's an if statement *inside* another if statement here, which is completely fine. The inner one only gets checked when the outer question was `true`, so a dead `Player` skips the whole thing.

Your version doesn't have to match this one. If `health` can't go above `maxHealth` and dead players still can't heal, it works.

</details>
