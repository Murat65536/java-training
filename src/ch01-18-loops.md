# Loops

## Doing something until
Say `bob` wants to smash a crate bare-handed. A punch does 1 damage and a crate has 40 health. Writing `bob.attack(crate);` 40 times would be silly. And sometimes you don't know ahead of time how many hits it will take, like when a weapon weakens with every hit.
The instruction we want to give is "keep attacking until the crate breaks." A **`while` loop** does exactly that:
```java
Player bob = new Player(100);
Crate crate = new Crate();
while (crate.getHealth() > 0) {
	bob.attack(crate);
}
```

| `while`                  | `(crate.getHealth() > 0)` | `{ bob.attack(crate); }`              |
| ------------------------ | ------------------------- | ------------------------------------- |
| Keep doing this, as long as... | ...this is `true`  | The code to run each time             |

A `while` loop starts out like an `if` statement. It checks the question, and if the answer is `true`, it runs the code inside the `{ }`. The difference is what comes next. An `if` is finished at that point. A `while` goes back to the top and asks the question *again*, and as long as the answer stays `true`, it keeps running the code. Once the answer is `false`, it moves on to whatever follows the loop.
Each trip through a loop is called an **iteration**.

To count how many hits it takes:
```java
int hits = 0;
while (crate.getHealth() > 0) {
	bob.attack(crate);
	hits++;
}
System.out.println("hits: " + hits);
```
```
hits: 40
```
`hits++` is short for `hits = hits + 1`. Adding 1 comes up so often that Java has its own short way to write it. `hits--` is the same as `hits = hits - 1`.

If the question is `false` the very first time, the code inside never runs. If the crate were already broken, this loop wouldn't attack at all.

## Forever
What if the question never becomes `false`?
```java
bob.setStance(Stance.DEFENDING);
while (crate.getHealth() > 0) {
	bob.attack(crate);
}
```
Defending players can't attack, so the crate never takes damage and its health stays above 0. The loop goes around forever, and the program never gets past it. That's an **infinite loop**.
If your program ever seems stuck with nothing happening, an infinite loop is the likely cause. To stop it, click in the terminal at the bottom of VS Code and press **Ctrl+C**.

> [!WARNING]
> Every loop has to make progress toward its question becoming `false`. Before you write a `while` loop, ask yourself what changes on each trip through, and how you know that change will eventually end the loop. If you can't answer, your loop might be able to run forever.

## Counting
Often you just want to do something a set number of times. Here's `bob` punching `alice` 3 times with a `while` loop:
```java
int i = 0;
while (i < 3) {
	bob.attack(alice);
	i++;
}
```
This comes up so often that Java has a loop just for it, the **`for` loop**. It does the same thing with all the counting on one line:
```java
for (int i = 0; i < 3; i++) {
	bob.attack(alice);
}
```
```java
System.out.println("alice: " + alice.getHealth());
```
```
alice: 97
```

| `for (` | `int i = 0;`                | `i < 3;`                         | `i++`                                 | `)` |
| ------- | --------------------------- | -------------------------------- | ------------------------------------- | --- |
|         | Do this once, at the start | Keep going as long as this is `true` | Do this at the end of every iteration |     |

Semicolons separate the three parts, and there's no `;` after the last one, `i++`.
`i` is the **loop variable**. It counts how many times the loop has gone around: 0, then 1, then 2. When it reaches 3, `i < 3` is `false` and the loop stops. That's 3 iterations, for 0, 1 and 2. You can use `i` inside the loop:
```java
for (int i = 0; i < 3; i++) {
	System.out.println("i is " + i);
}
```
```
i is 0
i is 1
i is 2
```
Programmers almost always call the loop variable `i` and start it at 0. The next lesson shows why starting at 0 is handy.

`i` only exists inside its loop. After the loop's `}` it's gone, and using it gets a red squiggly line saying `i cannot be resolved to a variable`. That's also why the next loop can use the name `i` again, as the examples above do.

### Loops in methods
Loops can go anywhere other code can, methods included. This one lets a `Player` attack something several times in a row:
```java
public void attackTimes(Damageable target, int times) {
	for (int i = 0; i < times; i++) {
		attack(target);
	}
}
```
```java
Player carl = new Player(100);
bob.attackTimes(carl, 10);
System.out.println("carl: " + carl.getHealth());
```
```
carl: 90
```
`attackTimes` doesn't need to know how attacking works. It calls `attack` as many times as it's told.

{{#quiz ../quizzes/ch01-18-loops.toml}}

## Which loop?
Either loop can do anything the other can. As a rule:
- Use a **`for`** loop when you know before you start how many times to go around, like "attack 3 times."
- Use a **`while`** loop when you're waiting for something to happen and don't know how long it'll take, like "attack until the crate breaks."

## Loops on a robot
Loops work a little differently on a robot, because the robot is *already* running a loop. About 50 times a second, WPILib gives every part of your code a turn. If one part starts its own loop and never pauses to let the others go, the whole robot gets stuck waiting.
Here's how that happens. Robot code is organized into **commands**, which are things for the robot to do, like "raise the arm" or "drive forward." (You'll write plenty of them later.) Say you want a command that raises the arm until it reaches the scoring position. A loop looks like the right tool:
```java
public Command raiseToScoring() {
	return run(coroutine -> {
		while (getAngle() < ArmPosition.SCORING.getAngleDegrees()) {
			setSpeed(ArmConstants.MAX_SPEED);
		}
		setSpeed(0);
	}).named("Arm.RaiseToScoring");
}
```
`run(...)` makes a command for the arm, and the lambda is what the command does. The problem is that robot code does one thing at a time, so while this loop runs, *nothing else does*. No other command gets a turn. The code stops reading the driver's controller, so the driver can't drive. The robot is stuck until the arm arrives, and if it never does, say because the arm is jammed, it's stuck for good. When this happens, the Driver Station fills up with warnings like `Watchdog not fed within 0.020000s`, meaning an update took longer than its 20 milliseconds.

The fix is one line:
```java
public Command raiseToScoring() {
	return run(coroutine -> {
		while (getAngle() < ArmPosition.SCORING.getAngleDegrees()) {
			setSpeed(ArmConstants.MAX_SPEED);
			coroutine.yield();
		}
		setSpeed(0);
	}).named("Arm.RaiseToScoring");
}
```
`coroutine.yield()` means "that's all for now. Let everything else have a turn, and come back to me next time." Each trip through, the command checks the arm, sets its speed and steps aside. About 20 milliseconds later, after everything else has had a turn, the loop picks up where it left off and goes around once more. The command takes as long as it takes, but the rest of the robot keeps running the whole time.

This mistake is common enough that WPILib checks for it. If a `while` loop in a command never calls `coroutine.yield()`, the robot code won't build, and you get an error saying ``Missing call to `coroutine.yield()` inside loop``.

> [!IMPORTANT]
> In robot code, every loop that waits for something has to call `coroutine.yield()` each time around, so the rest of the robot gets a turn.

Loops that finish right away don't need to yield. The next lesson has that kind of loop, going through a list of things like every wheel on the drivetrain.

{{#quiz ../quizzes/ch01-18-robot-loops.toml}}

## Putting it together
Here's everything now. Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-18}}

## Try it yourself
Give players a combo, a burst of attacks one right after another. Add a method `public int combo(Damageable target)` to `Player` that:
1. Keeps attacking `target` until it's knocked out.
2. Never attacks more than `MAX_COMBO_HITS` times in one combo, even if the target is still standing. Make `MAX_COMBO_HITS` 5.
3. Returns how many times it attacked.

*Hint: which loop fits "until it's knocked out"? Your loop also has to check two things each time. Think about what happens when `bob` punches a `Tank`, where a 1 damage punch turns into 0.*

To check your answer, make your changes and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-18/Game.java}}
```
If every line prints what its comment says, your combo works.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-18}}
There's no telling how many hits the target will take, so it's a `while` loop. It keeps going while *both* things are true: the target still has health, and the combo hasn't reached `MAX_COMBO_HITS`. `hits` does two jobs. It tells the loop when to stop, and it's the answer `combo` returns.

Without the `hits < Constants.MAX_COMBO_HITS` part, a bare-handed combo on a `Tank` would be an infinite loop. Each punch does 1 damage, the armor turns that into 0, and the `Tank`'s health never drops. A limit like this is a good safety net for any `while` loop that might not finish.

</details>
