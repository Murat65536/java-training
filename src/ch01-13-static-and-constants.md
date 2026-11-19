# Static and Constants

## One for everyone
Suppose we want to count how many players have been made in the whole game. The constructor runs once for every new `Player`, so that's a good place to count:
```java
private int playersCreated;

public Player(int startingHealth) {
	health = startingHealth;
	maxHealth = startingHealth;
	playersCreated = playersCreated + 1;
}

public int getPlayersCreated() {
	return playersCreated;
}
```
```java
Player bob = new Player(100);
Player alice = new Player(100);
System.out.println(alice.getPlayersCreated());
```
```
1
```
Two players were made, but it says 1.
[Fields](ch01-04-fields.md) explained why: every `Player` gets *its own* copy of every field. `bob`'s `playersCreated` went from 0 to 1 when `bob` was made. `alice` got a separate `playersCreated`, which also went from 0 to 1. Neither knows about the other.

We want *one* `playersCreated` that every `Player` shares. **`static`** does that:
```java
private static int playersCreated;
```
```
2
```
In the blueprint picture, a normal field is like a front door: every house has its own. A `static` field is written on the *blueprint itself*. There's one blueprint, so there's one `playersCreated`, however many houses get built. `bob`'s constructor adds 1, then `alice`'s constructor adds 1 to the same number.

A `static` field is also called a **class variable**, since it belongs to the class instead of to one object. (This is the "something else" from the footnote in [Fields](ch01-04-fields.md).)

## Static methods
`alice.getPlayersCreated()` is still a bit strange. How many players exist isn't a question about `alice`. And if we wanted to ask before *any* players existed, there'd be no `Player` to ask.
Methods can be `static` too:
```java
public static int getPlayersCreated() {
	return playersCreated;
}
```
A `static` method belongs to the class, like a `static` field. You call it on the class by name instead of on an object:
```java
System.out.println(Player.getPlayersCreated());
Player bob = new Player(100);
Player alice = new Player(100);
System.out.println(Player.getPlayersCreated());
```
```
0
2
```

### What static methods can't do
A `static` method isn't called on any object, so it can't use normal fields. Try this:
```java
public static boolean isHurt() {
	return health < maxHealth;
}
```
The red squiggly line says `Cannot make a static reference to the non-static field health`. Java is asking "*whose* health?" `Player.isHurt()` isn't called on `bob` or `alice` or anyone, so there's no `health` to check. `getPlayersCreated` works because `playersCreated` is `static` too. There's only one, so Java knows which one you mean.
Calling normal methods has the same problem. A `static` method can't call `isAlive()` on its own, because there's no object to ask.

To decide whether something should be `static`, ask whether it's about *one* object or the whole class. A `Player`'s health is about one player, so it's a normal field. The number of players is about all of them, so it's `static`.

> [!WARNING]
> `static` fields that change, like `playersCreated`, are rare in robot code. Every object shares them, so any object can change them for everyone else, and that makes bugs hard to track down. Most `static` fields you'll see are constants, which never change. Constants come up later in this lesson.

{{#quiz ../quizzes/ch01-13-static.toml}}

## Static all around you
Back to the template once more:
```java
class Game {
	public static void main(String[] args) {
		// Your code goes here!
	}
}
```
`main` is `static`. When the program starts there are no objects at all, not even a `Game`, so Java calls `main` on the class, the way we called `Player.getPlayersCreated()`. That finishes `public static`: `public` lets Java call it from outside `Game`, and `static` lets Java call it without making a `Game` first. (The last piece, `String[] args`, waits until [Lists](ch01-19-lists.md).)

This also explains a red squiggly line you may have already hit. Say you want a method in `Game` that prints a `Player`'s status, so you don't keep writing the same `println`:
```java
class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		printStatus(bob);
	}

	void printStatus(Player p) {
		System.out.println("health: " + p.getHealth() + ", x: " + p.getX());
	}
}
```
The call to `printStatus` gets a red squiggly line saying `Cannot make a static reference to the non-static method`. `main` is `static`, so there's no `Game` object to call `printStatus` on. Making `printStatus` `static` as well fixes it:
```java
static void printStatus(Player p) {
	System.out.println("health: " + p.getHealth() + ", x: " + p.getX());
}
```
```
health: 100, x: 0
```
Any method you add to `Game` and call from `main` has to be `static`.

### Java's static methods
Java comes with plenty of `static` methods. Some of the handiest are in a class called `Math`:
```java
System.out.println(Math.max(3, 7));
System.out.println(Math.min(3, 7));
System.out.println(Math.abs(-5));
```
```
7
3
5
```
`Math.max` returns the bigger of two numbers and `Math.min` returns the smaller. `Math.abs` returns a number without its minus sign, which is called its *absolute value*. You never write `new Math()`. The bigger of two numbers is the same no matter what, so there's nothing for an object to hold. You call the methods on the class.

`System.out.println` uses `static` too. `System` is a class that comes with Java, and `out` is a `static` field inside it. That field holds the object that prints to the screen, and `println` is one of that object's methods. So `System.out.println(...)` means "get `out` from the `System` class and call its `println`."

## Constants
Here's `move`:
```java
public void move(int steps) {
	if (isAlive() && steps >= -10 && steps <= 10) {
		x = x + steps;
		if (x < 0) {
			x = 0;
		} else if (x > 100) {
			x = 100;
		}
	}
}
```
What are `10` and `100`? We know `10` is the most steps a player can take at once and `100` is the edge of the arena, because we wrote it. Someone reading it for the first time has to work that out. Numbers like these are called **magic numbers**, because they appear with no explanation.
They cause a second problem. Say we want a bigger arena. `100` appears in `move` and also in `isAtEdge`, so we'd have to find every `100` in the code and change the right ones. Miss one, and players can walk past the edge without ever being at it.

So we give the numbers names, by adding these to `Player`:
```java
public static final int ARENA_SIZE = 100;
public static final int MAX_STEPS = 10;
```
A field that's both `static` and `final` is a **constant**. Each word is there for a reason:
- `final`, because the arena's size should never change while the game runs.
- `static`, because every `Player` uses the same arena. There's no point in each `Player` carrying its own copy of 100.
- `public`, so other code can use it. The last lesson said fields should be `private` so nobody changes them without going through our methods. Nobody can change a `final` field at all, though, so letting anyone read it is safe.

Constant names are written in ALL CAPS with `_` between words. When you see `ARENA_SIZE` in the middle of some code, you can tell right away that it's a constant and not a normal variable.

Now `move` says what it means:
```java
public void move(int steps) {
	if (isAlive() && steps >= -MAX_STEPS && steps <= MAX_STEPS) {
		x = x + steps;
		if (x < 0) {
			x = 0;
		} else if (x > ARENA_SIZE) {
			x = ARENA_SIZE;
		}
	}
}
```
`-MAX_STEPS` works like it does in math. It means "negative `MAX_STEPS`," which is `-10`.
A bigger arena is now a one-line change.

Which numbers need a name? Almost all of them. Any number that's a *rule* of your game or a fact about your robot gets a name, even if it only appears once. When in doubt, name it.
There are a few exceptions. The `0` in `x < 0` is "the left edge," and `0` health is "knocked out." `0` means the same thing in any game, so it can stay `0`. Adding 1 to count something, like `score = score + 1`, is the same. Those numbers need no explanation and will never change.

## A class for constants
Once a program has many constants, it helps to keep them in one place. Robot code puts them in classes that exist only to hold constants. Ours is called `Constants`, in its own file `Constants.java`. It never makes any objects. It's a list of names and values:
```java
public final class Constants {
	public static final int ARENA_SIZE = 100;
	public static final int MAX_STEPS = 10;
	public static final int DANGER_HEALTH = 20;
	public static final int WEAPON_DURABILITY = 3;
	public static final int PUNCH_DAMAGE = 1;
	public static final int HEALER_HEAL_AMOUNT = 20;
	public static final int TANK_ARMOR = 5;
}
```
Look at the `final` in `public final class Constants`. On a field, `final` means it can't be changed. On a class, it means no other class can `extend` it. Nothing should ever be a child class of `Constants`, and `final` guarantees that. Every constants class in robot code is `final` too.

To use a constant from another class, put the class's name in front, as with `Player.getPlayersCreated()`:
```java
public boolean isAtEdge() {
	return x == 0 || x == Constants.ARENA_SIZE;
}

public boolean isInDanger() {
	return isAlive() && health < Constants.DANGER_HEALTH;
}
```
Constants are `final`, so nothing can change them while the program runs. This gets a red squiggly line saying `The final field Constants.ARENA_SIZE cannot be assigned`:
```java
Constants.ARENA_SIZE = 200;
```
For a bigger arena, change the `100` in `Constants` and run the program again.

The rest of the game's rules get names the same way. `Weapon`'s constructor sets `durability = Constants.WEAPON_DURABILITY;` instead of `durability = 3;`, and a punch in `attack` does `Constants.PUNCH_DAMAGE` instead of `1`. `Healer` and `Tank` use their constants too:
```java
public void healFriend(Player friend) {
	if (isAlive()) {
		friend.heal(Constants.HEALER_HEAL_AMOUNT);
	}
}
```
```java
@Override
public void takeDamage(int amount) {
	if (amount > Constants.TANK_ARMOR) {
		super.takeDamage(amount - Constants.TANK_ARMOR);
	}
}
```
`Tank` used the `5` *twice*. If someone made armor stronger and only changed one of them, `Tank` would do the wrong thing with no warning. With a constant there's only one number to change.

### Constants on a robot
The arm from the last lesson had two magic numbers:
```java
private final SparkMax motor = new SparkMax(CANPort.CAN_S0, 5, MotorType.kBrushless);
```
```java
if (speed > 0.5) {
```
The `5` is the arm motor's ID. Every motor controller on the robot has its own ID number so the code can tell them apart. The `0.5` is the arm's top speed. (`CANPort.CAN_S0` isn't a magic number. It already has a name that says what it is: the first CAN connector on the robot's controller.)
A robot has far more constants than our game, so robot code gives each part of the robot its own constants class, like `ArmConstants` and `DriveConstants`. They all live in a folder named `constants`:
```java
public final class ArmConstants {
	public static final int MOTOR_ID = 5;
	public static final double MAX_SPEED = 0.5;
}
```
```java
private final SparkMax motor = new SparkMax(CANPort.CAN_S0, ArmConstants.MOTOR_ID, MotorType.kBrushless);
```
```java
if (speed > ArmConstants.MAX_SPEED) {
```
The names don't start with `ARM_`, because the `ArmConstants.` in front already says they're about the arm.

On a real team this saves a lot of trouble. If the robot gets rewired and the arm motor becomes ID 7, or the arm turns out to be safe at a top speed of 0.6, you change one line in `ArmConstants` instead of hunting through the rest of the code.

You'll sometimes see constants in older robot code named with a `k` in front, like `kArmMotorID` instead of `ARM_MOTOR_ID`. WPILib used to use that style, and it's obsolete in 2027 projects and later. It means the same thing, so you'll recognize it when you see it. (That's what the `k` in `MotorType.kBrushless` is.) When you write your own constants, use ALL CAPS.

{{#quiz ../quizzes/ch01-13-constants.toml}}

## Putting it together
Here's everything now. Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-13}}

## Try it yourself
`takeDamage`, `heal` and `move` each use an `if` to keep a number within limits. Rewrite those three methods to use `Math.max` and `Math.min` instead. When you're done, none of them should need an `if` to keep a number in range. (`heal` and `move` still need their `if (isAlive() ...)` checks.)

*Hint: what's the smallest number `Math.max(0, something)` can return? For `move`, you can use `Math.min` and `Math.max` together by putting one inside the other's `()`.*

To check your answer, make your changes and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-13/Game.java}}
```
If every line prints what its comment says, your new versions behave like the old ones.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-13}}
`Math.max(health - amount, 0)` returns whichever is bigger, the new health or 0. If the new health would be below 0, you get 0 instead. `Math.min(health + amount, maxHealth)` does the same from the other side. It can never return more than `maxHealth`.

The one in `move` reads best from the inside out:
1. `Math.min(x + steps, Constants.ARENA_SIZE)` keeps the new `x` from going past 100.
2. `Math.max(0, ...)` then keeps *that* from going below 0.

So `x` always ends up between 0 and 100. A `max` wrapped around a `min` like this is the standard way to keep a number between two limits, and you'll write it often in robot code.

</details>
