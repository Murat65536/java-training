# Inheritance

## Different kinds of players
Every `Player` in our game works the same way right now. Most games have different kinds of characters, so we'll add a **healer**: a `Player` who can also heal their friends.
One way to do it would be a brand new `Healer` class with everything from `Player` copied into it. That's a lot of copying, though. Worse, every bug we fixed in one class would also have to be fixed in the other, and sooner or later someone would forget.
A better way is to say that a `Healer` *is* a `Player` with a little extra added. That's called **inheritance**, and the keyword for it is `extends`. `Healer` goes in its own file, `Healer.java`:
```java
class Healer extends Player {
	void healFriend(Player friend) {
		if (isAlive()) {
			friend.heal(20);
		}
	}
}
```

| `class Healer`    | `extends Player`                                            |
| ----------------- | ----------------------------------------------------------- |
| A new class | Start with everything `Player` has, then add what's below |

`Player` is the **parent class** of `Healer`, and `Healer` is a **child class** of `Player`. A `Healer` has every field and method a `Player` has: `health`, `takeDamage`, `move`, `attack` and the rest. That's why `healFriend` can call `isAlive()` even though `Healer` never defines it. The method comes from `Player`.

If you type this in, though, you'll get red squiggly lines. Constructors are the one thing a child class doesn't inherit. A `Player` can't be built without a starting health, and a `Healer` is a `Player`, so the same goes for it. `Healer` needs a constructor of its own that hands the starting health to `Player`'s constructor:
```java
class Healer extends Player {
	Healer(int startingHealth) {
		super(startingHealth);
	}

	void healFriend(Player friend) {
		if (isAlive()) {
			friend.heal(20);
		}
	}
}
```
`super(...)` runs the parent's constructor with the values you give it. Here `super(startingHealth);` runs `Player`'s constructor, which sets `health` and `maxHealth` the way it does for every `Player`.
(The red squiggly line from before mentioned a "super constructor" if you hovered over it. This is what it meant.)

To try it out:
```java
Healer hana = new Healer(100);
Player bob = new Player(100);
bob.takeDamage(50);
hana.healFriend(bob);
System.out.println("bob: " + bob.health);
hana.takeDamage(30);
System.out.println("hana: " + hana.health);
```
```
bob: 70
hana: 70
```
`hana` can heal, and `hana` can still do anything a normal `Player` does, including taking damage.

Robot code uses `extends` too. The class for the whole robot starts with `public class Robot extends OpModeRobot`. WPILib's developers wrote `OpModeRobot`, and it has everything a robot program needs to start up and keep running. Our `Robot` extends it and adds the parts that only our robot has.

{{#quiz ../quizzes/ch01-11-extends.toml}}

## Doing things differently
A child class can also change how an inherited method works. Our next character is a **tank**, a `Player` with armor. Every hit does 5 less damage, so a hit of 5 or less does nothing at all.
`Tank` gets this by writing its own `takeDamage`:
```java
class Tank extends Player {
	Tank(int startingHealth) {
		super(startingHealth);
	}

	@Override
	void takeDamage(int amount) {
		if (amount > 5) {
			super.takeDamage(amount - 5);
		}
	}
}
```
Replacing a parent's method with your own version is called **overriding** it. Any time something calls `takeDamage` on a `Tank`, Java runs `Tank`'s version and not `Player`'s.

Two things in there are new:
- **`@Override`** sits right above a method that replaces one from the parent class. It asks Java to check that the method really does replace something. Say you misspelled it as `takeDamge`. Without `@Override` you'd get a brand new method that nothing ever calls, and no warning about it. With it you get a red squiggly line. It's optional, but always write it.
- **`super.takeDamage(amount - 5)`** calls `Player`'s version of `takeDamage`. `super(...)` meant "my parent's constructor," and `super.` means "my parent's version of this method." So `Tank` doesn't copy any of `Player`'s code. It shrinks the amount and lets `Player`'s version handle the rest, including keeping health from dropping below 0.

```java
Tank tom = new Tank(100);
tom.takeDamage(20);
System.out.println("tom: " + tom.health);
tom.takeDamage(3);
System.out.println("tom: " + tom.health);
```
```
tom: 85
tom: 85
```
The 20 damage hit only took 15. The 3 damage hit did nothing.

> [!WARNING]
> Inside `Tank`'s `takeDamage`, the call has to be `super.takeDamage(...)` and not plain `takeDamage(...)`. Without `super.` the method calls itself, which calls itself again, and so on forever, until Java gives up and crashes with a `StackOverflowError`. If you ever see that crash, look for a method that calls itself by mistake.

## A Tank is a Player
Now look at `attack` again. It takes a `Player`:
```java
void attack(Player target) {
```
A `Tank` is a `Player`, so a `Tank` can be attacked:
```java
Tank tom = new Tank(100);
Player bob = new Player(100);
bob.equip(new Weapon("Sword", 25));
bob.attack(tom);
System.out.println("tom: " + tom.health);
```
```
tom: 80
```
The sword does 25 damage, but `tom` only lost 20. `target` was a `Tank`, so when `attack` called `target.takeDamage(weapon.damage)`, Java ran `Tank`'s version.
We wrote `attack` before `Tank` existed, and it still handles tanks correctly. That's because Java always runs the version of a method that belongs to the actual object, whatever type of variable you used to get to it.

Robot code depends on this. `OpModeRobot` has a method called `robotPeriodic()`, and WPILib calls it about 50 times a second while the robot is on. `OpModeRobot`'s version is empty. Our `Robot` overrides it with whatever our robot needs to do over and over, like checking its sensors. WPILib's code was written long before our robot existed and knows nothing about it. It calls `robotPeriodic()`, the object is our `Robot`, and so our version runs.

{{#quiz ../quizzes/ch01-11-override.toml}}

## Putting it together
This is everything so far. `Player` hasn't changed since last lesson except for `attack`, which now wears out weapons (that was last lesson's exercise). Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-11}}

## Try it yourself
Make a new kind of `Player` called `Berserker`. A `Berserker` attacks like any other `Player`, except while they're in danger. Then every attack happens *twice*.

*Hint: you don't need to copy any of `attack`'s code. How can a child class run its parent's `attack`?*

To check your answer, put `Berserker` in a new file, `Berserker.java`, and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-11/Game.java}}
```
If every line prints what its comment says, your `Berserker` works. The last part makes sure a normal `Player` in danger still attacks only once.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-11}}
A `Berserker` attack runs `Player`'s `attack` once, then runs it again if the `Berserker` is in danger. None of `attack`'s code was copied, so any future change to how attacking works applies to `Berserker` automatically.

</details>
