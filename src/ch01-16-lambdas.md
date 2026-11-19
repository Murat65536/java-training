# Lambdas

## This
In the last exercise, `useAbility` had to tell the ability which `Player` was using it:
```java
public void useAbility(Damageable target) {
	if (isAlive() && ability != null) {
		ability.use(this, target);
	}
}
```
The `Player` using the ability is whichever `Player` `useAbility` was called on. The question is how to refer to it.
**`this`** is the answer. It means "the object this method was called on." When `bob.useAbility(alice)` runs, `this` is `bob`, so `ability.use(this, target)` hands `bob` to the ability as its `user`.

`this` showed up once before, in [Reading the error](ch01-10-objects-inside-objects.md#reading-the-error), where a crash said `this.weapon` was null. You've also been using it all along without seeing it. Inside a method, `health` is short for `this.health`, the health of the object the method was called on. Java fills in `this.` for you, so you only write it when you need to refer to the object itself, as `useAbility` does.

## A shortcut
`Fireball` is 6 lines long, and only one of them does any work:
```java
target.takeDamage(Constants.FIREBALL_DAMAGE);
```
The rest is there because Java needs a class to put that line in. A game with dozens of abilities would need dozens of classes like it.
Java has a shortcut for this called a **lambda**:
```java
bob.learn((user, target) -> target.takeDamage(Constants.FIREBALL_DAMAGE));
```
That one line does the same job as the whole `Fireball` class:
```java
Player bob = new Player(100);
Player alice = new Player(100);
bob.learn((user, target) -> target.takeDamage(Constants.FIREBALL_DAMAGE));
bob.useAbility(alice);
System.out.println("alice: " + alice.getHealth());
```
```
alice: 70
```

### Reading a lambda

| `(user, target)`                     | `->`              | `target.takeDamage(Constants.FIREBALL_DAMAGE)` |
| ------------------------------------ | ----------------- | ---------------------------------------------- |
| The parameters, just like a method's | "Do this with them" | The code to run                               |

A lambda is a method with no name, written right where it's needed.
The parameters have no types, though. `user` should be a `Player` and `target` a `Damageable`, and Java works that out by itself. `learn` takes an `Ability`, and `Ability` has exactly one method, `use(Player user, Damageable target)`. So the lambda must *be* that method, and its parameters must have those types.

That's also why lambdas only work for interfaces with exactly **one** method. With two methods, Java couldn't tell which one your lambda was meant to be. Try making a `Damageable` with a lambda and you'll get a red squiggly line saying `The target type of this expression must be a functional interface`. A **functional interface** is an interface with one method, like `Ability`. `Damageable` has two, so it isn't one.

You pick the parameter names, as with any method. `(u, t) -> t.takeDamage(Constants.FIREBALL_DAMAGE)` works the same, but names that say what things are make the code easier to read.

## Longer lambdas
`Drain` does two things, so it won't fit on one line. For longer code, wrap it in `{ }` like the inside of a method:
```java
bob.learn((user, target) -> {
	target.takeDamage(Constants.DRAIN_AMOUNT);
	user.heal(Constants.DRAIN_AMOUNT);
});
```
Each line inside the `{ }` ends with `;` as usual. The whole thing ends with `});`. The `}` closes the lambda, the `)` closes `learn(`, and the `;` ends the line.

## Lambdas are objects
A lambda makes an object, the same as `new Fireball()` did. You can keep one in a variable and use it as often as you want:
```java
Ability fireball = (user, target) -> target.takeDamage(Constants.FIREBALL_DAMAGE);
bob.learn(fireball);
alice.learn(fireball);
```
A lambda can even be a constant. Our abilities can live in a class of their own, `Abilities.java`, the way `Constants` holds our numbers:
```java
public class Abilities {
	public static final Ability FIREBALL = (user, target) -> target.takeDamage(Constants.FIREBALL_DAMAGE);

	public static final Ability DRAIN = (user, target) -> {
		target.takeDamage(Constants.DRAIN_AMOUNT);
		user.heal(Constants.DRAIN_AMOUNT);
	};
}
```
```java
Player carl = new Player(100);
carl.learn(Abilities.FIREBALL);
```
The `Fireball` and `Drain` classes aren't needed anymore.

{{#quiz ../quizzes/ch01-16-lambdas.toml}}

## Lambdas that give back an answer
`Ability`'s method returns nothing, but lambdas can return values too. This interface's one method answers a yes-or-no question. It goes in `Condition.java`:
```java
public interface Condition {
	boolean isMet();
}
```
```java
Condition inDanger = () -> bob.isInDanger();
System.out.println(inDanger.isMet());
```
`isMet` takes no parameters, so the lambda's `()` is empty. When a lambda is a single line, whatever that line works out to is what the lambda returns. So `inDanger.isMet()` returns whatever `bob.isInDanger()` is at the moment you ask.
With `{ }`, you need a `return`, like in a normal method:
```java
Condition inDanger = () -> {
	return bob.isInDanger();
};
```

Java comes with some functional interfaces of its own. One you'll use a lot is **`Runnable`**, for "something to do." Its one method is `void run()`, which takes no parameters:
```java
Runnable punch = () -> bob.attack(alice);
punch.run();
```
`Runnable` is built into Java, like `String`, so you don't have to write it.

## Triggers
`Condition` and `Runnable` combine nicely into a **trigger**: something that does an action whenever a condition is met. It's a class, so it goes in `Trigger.java`:
```java
public class Trigger {
	private final Condition condition;
	private final Runnable action;

	public Trigger(Condition triggerCondition, Runnable triggerAction) {
		condition = triggerCondition;
		action = triggerAction;
	}

	public void update() {
		if (condition.isMet()) {
			action.run();
		}
	}
}
```
A `Trigger` has no idea what its condition or action are. They're lambdas someone handed it. Each call to `update` asks the condition, and if the answer is yes, runs the action.
Here `bob` gets a rage. Whenever `bob` is in danger, `bob` lashes out at `alice`:
```java
Player bob = new Player(100);
Player alice = new Player(100);
Trigger rage = new Trigger(() -> bob.isInDanger(), () -> bob.attack(alice));

rage.update();
System.out.println("alice: " + alice.getHealth());

bob.takeDamage(85);
rage.update();
System.out.println("alice: " + alice.getHealth());
```
```
alice: 100
alice: 99
```
On the first `update`, `bob` wasn't in danger, so nothing happened. On the second, `bob` was, and `alice` got punched.
The lambdas use `bob` and `alice` even though those are variables from `main`. A lambda can use variables from the code around where it was written. That's a big part of their usefulness: the trigger can do something to `alice` without ever knowing who `alice` is.

## Method references
A lambda that only calls one method can be written even shorter:
```java
Trigger rage = new Trigger(bob::isInDanger, () -> bob.attack(alice));
```
`bob::isInDanger` is a **method reference**. It means "a lambda that calls `bob.isInDanger()`," so it's the same as `() -> bob.isInDanger()`. There's no `()` after `isInDanger`, because nothing is being called yet. The method is being handed over so the trigger can call it later.
A method reference only works when the lambda's parameters go straight into the method and nothing else is added. `() -> bob.attack(alice)` passes `alice`, which didn't come from the lambda's `()`, so it has to stay a lambda. This one works, though:
```java
Runnable comeBack = bob::respawn;
```

> [!WARNING]
> A lambda can only use a variable from the code around it if that variable never changes after it's set. This gets a red squiggly line saying `Local variable target defined in an enclosing scope must be final or effectively final`:
> ```java
> Player target = alice;
> Runnable punch = () -> bob.attack(target);
> target = carl;
> ```
> "Defined in an enclosing scope" means `target` was made outside the lambda, in the surrounding code. (See [scope](ch01-07-if-statements.md#where-variables-live).) The lambda might not run until much later, so Java couldn't tell whether you meant the old `target` or the new one. "Effectively final" means "never changed," even without the word `final`. If you need a different target, use a different variable.

## Lambdas on a robot
So much of robot code is "when this happens, do that" that lambdas are everywhere in it. WPILib has its own `Trigger` class that works a lot like ours. It's how a button on the controller gets connected to something the robot does, like "when the driver presses this button, raise the arm." Its condition can be anything you like:
```java
Trigger armTooHigh = new Trigger(() -> arm.getAngle() > ArmConstants.MAX_ANGLE_DEGREES);
```
WPILib's `Trigger` takes a `BooleanSupplier` instead of a `Condition`. `BooleanSupplier` is Java's own functional interface for "a yes-or-no answer you can ask for later." It's the same idea as our `Condition`, except its one method is called `getAsBoolean`. Like our `update`, WPILib checks every trigger over and over while the robot runs, about 50 times a second.

`DoubleSupplier` is the same thing for a `double` you can ask for later. Reading joysticks uses it:
```java
DoubleSupplier forwardSpeed = () -> -controller.getLeftY();
```
Each time the code asks `forwardSpeed` for a number, it gets where the driver's joystick is *right now*. (Pushing a joystick forward gives a negative number, so the `-` flips it.) Storing `-controller.getLeftY()` in a plain `double` would only tell us where the joystick was at the very start.

Method references show up all over robot code too, like `arm::stop`, wherever something needs "a thing to do" and a method already does it.
(Robot code files start with lines like `import java.util.function.BooleanSupplier;`, which tell Java where to find things like `BooleanSupplier`. [Lists](ch01-19-lists.md) covers those.)

{{#quiz ../quizzes/ch01-16-method-references.toml}}

## Putting it together
Here's everything now. `Abilities` has replaced the `Fireball` and `Drain` classes. Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-16}}

## Try it yourself
The rage trigger has a problem. It attacks on *every* `update` while `bob` is in danger:
```java
rage.update();
rage.update();
rage.update();
```
That's three attacks, though `bob` only got into danger once. A real game would run `update` many times a second, and `alice` would be knocked out almost instantly.
Change `Trigger` so the action runs only when the condition *changes* from not met to met. After that, it shouldn't run again until the condition stops being met and then becomes met again.

*Hint: knowing whether the condition just changed means knowing what it was the last time `update` ran. Where can a `Trigger` keep something between calls?*

To check your answer, make your changes and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-16/Game.java}}
```
If every line prints what its comment says, your trigger only fires when it should.
WPILib's `Trigger` has this same distinction. You can run something *while* a button is held, or only *once*, at the moment it's pressed.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-16}}
`wasMet` remembers what the condition was on the previous `update`. The action runs only when the condition is met *now* and wasn't *then*. Afterward, `wasMet` is set to this time's answer, ready for the next `update`.
`wasMet` starts out `false`, like every `boolean` field. So if the condition is already met on the very first `update`, that counts as a change, and the action runs.

</details>
