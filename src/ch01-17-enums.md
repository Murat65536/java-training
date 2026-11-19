# Enums

## One of a few choices
Players are getting a **stance**, which is how they're fighting at the moment. A `Player` can be in one of three:
- **Normal**, which is how players have worked so far.
- **Defending**, which means they take half damage but can't attack.
- **Aggressive**, which means they hit harder but also take more damage.

How should `Player` remember its stance? A `String` is one option:
```java
private String stance = "normal";
```
But nothing would stop someone from writing `bob.setStance("defnding")`. That's a typo, but it's still a `String`, so there's no red squiggly line. Code that checks for `"defending"` would never match it, and `bob` would go on fighting like a normal player with no sign anything was wrong. Someone could also write `bob.setStance("sleeping")`, which isn't a stance at all.
Numbers are another option, like 0 for normal, 1 for defending and 2 for aggressive. Then everyone reading the code has to remember what `1` means, and nothing stops someone from setting it to `7`.

What we want is a type with *exactly* three possible values, where anything else is a red squiggly line.

## Enums
That type is called an **enum**, short for "enumeration," which means a list of things.
```java
public enum Stance {
	NORMAL,
	DEFENDING,
	AGGRESSIVE
}
```
This makes a new type, `Stance`, and lists every value it can ever have. The values are constants, so they're named in ALL CAPS like the ones in `Constants`. An enum gets its own file like a class or interface, so this goes in `Stance.java`.

To use a value, put the enum's name in front, as in `Stance.DEFENDING`. A `Stance` variable can hold any of the three:
```java
private Stance stance = Stance.NORMAL;
```
```java
public Stance getStance() {
	return stance;
}

public void setStance(Stance newStance) {
	stance = newStance;
}
```
(A setter is fine this time. Every `Stance` is a valid stance, so there's nothing to protect.)
Now a typo like `Stance.DEFNDING` gets a red squiggly line saying `DEFNDING cannot be resolved or is not a field`. `bob.setStance("sleeping")` gets one too, since a `String` isn't a `Stance`. It's impossible to make a stance that isn't one of the three.

Printing an enum value prints its name:
```java
Player bob = new Player(100);
bob.setStance(Stance.DEFENDING);
System.out.println(bob.getStance());
```
```
DEFENDING
```

You check an enum's value with `==`, as with numbers. Defending players can't attack, so `attack` gets one more check:
```java
public void attack(Damageable target) {
	boolean wasAlive = target.getHealth() > 0;
	if (isAlive() && stance != Stance.DEFENDING) {
		// ...everything else stays the same
	}
}
```

## Switch
Next is damage. Each stance takes a different amount, so `takeDamage` has to pick a multiplier based on the stance:
```java
public static final double DEFENDING_DAMAGE_MULTIPLIER = 0.5;
public static final double AGGRESSIVE_DAMAGE_MULTIPLIER = 1.5;
```
`else if`s would do it:
```java
double multiplier;
if (stance == Stance.NORMAL) {
	multiplier = 1.0;
} else if (stance == Stance.DEFENDING) {
	multiplier = Constants.DEFENDING_DAMAGE_MULTIPLIER;
} else {
	multiplier = Constants.AGGRESSIVE_DAMAGE_MULTIPLIER;
}
```
Java has a tool built for this, though, called a **switch**:
```java
@Override
public void takeDamage(int amount) {
	double multiplier = switch (stance) {
		case NORMAL -> 1.0;
		case DEFENDING -> Constants.DEFENDING_DAMAGE_MULTIPLIER;
		case AGGRESSIVE -> Constants.AGGRESSIVE_DAMAGE_MULTIPLIER;
	};
	health = Math.max(health - (int) (amount * multiplier), 0);
}
```

| `switch (stance)`         | `case DEFENDING`              | `->`             | `Constants.DEFENDING_DAMAGE_MULTIPLIER` |
| ------------------------- | ----------------------------- | ---------------- | --------------------------------------- |
| Look at what `stance` is | If it's `DEFENDING`... | ...then the answer is | This                                  |

The `switch` looks at `stance`, finds the matching `case`, and gives back whatever comes after that case's `->`. Inside a `case` you don't write `Stance.`, because Java already knows every case is a `Stance`. Note the `;` after the switch's closing `}`. The whole switch is the value being stored in `multiplier`, so the line still has to end.
(The `1.0` doesn't need a name. Multiplying by 1 changes nothing, so it's as self-explanatory as a `0`.)

### Covering every case
The switch also checks your work. Leave out a case, like this:
```java
double multiplier = switch (stance) {
	case NORMAL -> 1.0;
	case DEFENDING -> Constants.DEFENDING_DAMAGE_MULTIPLIER;
};
```
and you get a red squiggly line saying `A Switch expression should cover all possible values`. A switch that gives back a value must have an answer for *every* value, and Java makes sure none is missing.
That helps later on. If you add a fourth stance someday, every switch on `Stance` that doesn't handle it gets a red squiggly line. Java gives you a list of every place to update, so you don't have to hunt for them.

When several values share an answer, put them in one `case` separated by commas. This is how aggressive players hit harder:
```java
public static final int AGGRESSIVE_ATTACK_BONUS = 5;
```
```java
private int attackBonus() {
	return switch (stance) {
		case AGGRESSIVE -> Constants.AGGRESSIVE_ATTACK_BONUS;
		case NORMAL, DEFENDING -> 0;
	};
}
```
`attackBonus` is `private` because only `Player` uses it. `attack` adds it to every hit:
```java
target.takeDamage(Constants.PUNCH_DAMAGE + attackBonus());
```
```java
target.takeDamage(weapon.getDamage() + attackBonus());
```
Defending players never attack, so `attackBonus` never runs for a defending `Player`. The switch still has to cover `DEFENDING`, because Java checks every value, not only the ones that come up in practice.

Some switches have a `default` case, meaning "any value not listed above." Avoid it with enums. When you add a new value, `default` covers it without a word, so Java doesn't warn you. Listing every value is what gets you the warning.

{{#quiz ../quizzes/ch01-17-enums.toml}}

### Switches that do things
A switch doesn't have to give back a value. It can also *do* something different for each case. Then there's no `=` in front, and each `case` runs a line of code:
```java
switch (bob.getStance()) {
	case NORMAL -> System.out.println("bob is fighting normally");
	case DEFENDING -> System.out.println("bob has their shield up");
	case AGGRESSIVE -> System.out.println("bob is charging in");
}
```
If a case needs more than one line, wrap them in `{ }`, like `case NORMAL -> { ... }`.

> [!WARNING]
> A switch that does things *doesn't* have to cover every value. Leave one out and Java says nothing. That case does nothing. So when you add a new value to an enum, these switches won't appear in Java's list of things to fix. Check them yourself.

You'll also see switches written the older way, with `case NORMAL:` instead of `case NORMAL ->` and a `break;` at the end of each case. They work almost the same, except that a forgotten `break` lets the code run on into the *next* case too, which causes sneaky bugs. Use `->`.

## Enums on a robot
Robot code uses enums constantly, for the same reason we did: lots of things on a robot can only be one of a few choices.

### Picking with a switch
Every FRC match has two alliances, red and blue, and the field is a mirror image of itself. The red alliance's goals are on one side and the blue alliance's are on the other. So a lot of robot code needs to know the robot's alliance. WPILib has an enum for it:
```java
public enum Alliance {
	RED,
	BLUE
}
```
(That's how WPILib really writes it. WPILib's enum values are in ALL CAPS, like ours.)
A switch can pick the right answer for each alliance. For example, where the robot should aim:
```java
double targetX = switch (alliance) {
	case RED -> FieldConstants.RED_GOAL_X_METERS;
	case BLUE -> FieldConstants.BLUE_GOAL_X_METERS;
};
```
WPILib tells your code which alliance it's on, so the same code works on either side of the field. And since this switch gives back a value, Java makes sure it covers both alliances.

### Enums with values
An enum can also store information about each of its values. Say an arm has a few positions it moves to:
```java
public enum ArmPosition {
	STOWED(0.0),
	INTAKE(15.0),
	SCORING(80.0);

	private final double angleDegrees;

	ArmPosition(double positionAngleDegrees) {
		angleDegrees = positionAngleDegrees;
	}

	public double getAngleDegrees() {
		return angleDegrees;
	}
}
```
The `( )` after each value is a call to the enum's constructor, so `SCORING(80.0)` makes `SCORING` with an `angleDegrees` of `80.0`. The list of values now ends with a `;`, because more code follows it. The constructor has no `public` because nothing else is allowed to make new `ArmPosition`s. The three in the list are the only ones that will ever exist.
Any code can now ask for a position's angle:
```java
arm.isAtAngle(ArmPosition.SCORING.getAngleDegrees());
```
Those numbers aren't magic numbers, even though they aren't in `Constants`. Each one has a name, like "the `SCORING` position's angle," and each is written in exactly one place. The enum *is* their home.

The arm's motor used `MotorType.kBrushless`. `MotorType` is an enum too, with two values, `kBrushed` and `kBrushless`, for the two kinds of motor a `SparkMax` can run.

{{#quiz ../quizzes/ch01-17-robot-enums.toml}}

## Putting it together
Here's everything now. Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-17}}

## Try it yourself
`takeDamage` and `attackBonus` each use a switch to pick a number for each stance. Those numbers really describe the stances, though, so they belong in `Stance`, the way `ArmPosition` holds its angles:
1. Give each `Stance` a damage multiplier and an attack bonus. `NORMAL` takes normal damage and gets no bonus. `DEFENDING` and `AGGRESSIVE` use the same numbers as `DEFENDING_DAMAGE_MULTIPLIER`, `AGGRESSIVE_DAMAGE_MULTIPLIER` and `AGGRESSIVE_ATTACK_BONUS`.
2. Give `Stance` the getters `getDamageMultiplier()` and `getAttackBonus()`.
3. Change `takeDamage` and `attack` to use them. When you're done, `takeDamage` won't need a switch, `attackBonus` won't be needed at all, and neither will those three constants in `Constants`.

*Hint: look at how `ArmPosition` is written. `Stance` needs two values per stance instead of one, so its constructor needs two parameters.*

To check your answer, make your changes and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-17/Game.java}}
```
This `main` prints the same thing before and after your changes, so run it twice: once before you start and once when you're done. If every line prints what its comment says both times, you've moved the numbers without changing how anything works.
After this, a new stance keeps all its numbers in one place, right next to its name.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-17}}
Each stance's numbers sit right next to its name, so `NORMAL(1.0, 0)` reads as "normal players take normal damage and get no bonus." The constructor stores them, and the getters hand them back.
`takeDamage` and `attack` ask the current stance for its numbers, so the switches are gone, along with the three constants they used. Defending players still can't attack, because `attack` still checks `stance != Stance.DEFENDING`. That rule is about what a stance *does*, not a number, so it stays in `attack`.
Adding a new stance is now one line in `Stance`, with no changes to `takeDamage` or `attack`.

</details>
