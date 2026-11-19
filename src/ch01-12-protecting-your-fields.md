# Protecting Your Fields

## Breaking the rules
We've worked hard to keep `Player` in a sensible state. `takeDamage` stops health at 0. `heal` stops it at `maxHealth`. `move` keeps `x` between 0 and 100.
None of that stops this:
```java
Player bob = new Player(100);
bob.health = 9999;
bob.x = -50;
System.out.println("bob: " + bob.health + " health at " + bob.x);
```
```
bob: 9999 health at -50
```
No red squiggly lines and no crash. Any code can reach into `bob` and change `bob`'s fields, skipping every check we wrote. The rules only hold if everyone remembers to use the methods, and someone eventually won't.

On a robot this matters far more than in a game. An arm might only be able to swing so far before it smashes into the rest of the robot. If the arm code checks for that, but other code can set the arm's motor directly, the check protects nothing.

## Private
The fix is to put **`private`** in front of a field:
```java
class Player {
	private int health;
	private int maxHealth;
	private int x;
	private Weapon weapon;

	// ...everything else stays the same
}
```
`private` means "only code inside this class can use this." `Player`'s own methods can still use `health` as much as they like, so `takeDamage`, `heal` and the rest work as before. Outside `Player`, though, this now gets a red squiggly line:
```java
bob.health = 9999;
```
Hover over it and VS Code says `The field Player.health is not visible`. "Not visible" is Java's way of saying "this is private, so you can't use it from here."

The *only* way to change a `Player`'s health now is through `takeDamage`, `heal` and the other methods. Health can't become 9999 or drop below 0, whoever writes the code that uses `Player`.

`private` limits a field to code in the same *class*, not the same *object*. Inside `Player`'s methods you could still write `target.health`, since that's code in `Player` too. Our `attack` calls `target.takeDamage(...)` instead, which keeps a `Tank`'s armor working.

### Private fields and child classes
"Only code inside this class" means *only* `Player`. Child classes are shut out too:
```java
class Healer extends Player {
	// ...

	void fullHeal() {
		health = maxHealth;
	}
}
```
This gets a red squiggly line even though `Healer` extends `Player`. A `Healer` still *has* a `health` field, since it's a `Player`, but `Healer`'s code can't touch the field directly. It has to use `Player`'s methods like any other code:
```java
void fullHeal() {
	heal(missingHealth());
}
```
This is where `Tank`'s `super.takeDamage(...)` pays off. `Tank` never changed `health` itself, so it keeps working with a private `health` and needs no changes.

## Looking without touching
Private fields bring a new problem: other code can't *read* `bob.health` anymore either.
```java
System.out.println(bob.health);
```
That gets a red squiggly line too. A private field is locked for reading *and* writing.
Other code should be able to *see* a `Player`'s health. What it shouldn't do is change it without going through our methods. So `Player` gets a method that hands back the health:
```java
int getHealth() {
	return health;
}
```
```java
Player bob = new Player(100);
bob.takeDamage(30);
System.out.println("bob: " + bob.getHealth());
```
```
bob: 70
```
`getHealth` gives the caller a copy of the number without letting them change the field. A method that only hands back a field is called a **getter**, and its name is almost always `get` followed by the field's name. `x` gets one too:
```java
int getX() {
	return x;
}
```
There's no getter for `maxHealth`, and that's fine. Add a getter only when other code needs the value.
(You'll also come across methods named `set` plus a field's name, like `setHealth`. These are **setters**. Don't add one just because a getter exists. A `setHealth` that took any number would undo everything `private` just did for us.)

Robot code uses getters constantly, because that's how you ask the robot what's going on. A gyro is a sensor that tracks which way the robot is facing, and you ask it with `gyro.getAngle()`. A motor's encoder counts how far the motor has turned, and you ask it with `encoder.getPosition()`. Changing a field can't *set* the robot's angle. You read the angle with a getter, and you change it by turning the robot.

{{#quiz ../quizzes/ch01-12-private.toml}}

## Taking care of your own fields
`Weapon`'s fields should be private too:
```java
class Weapon {
	private String name;
	private int damage;
	private int durability;

	// ...the constructor stays the same
}
```
That gives `Player`'s `attack` red squiggly lines, because it reaches inside the weapon:
```java
target.takeDamage(weapon.damage);
weapon.durability = weapon.durability - 1;
if (weapon.durability == 0) {
	weapon = null;
}
```
That's a useful catch. Wearing out is something a *weapon* does, so the code for it belongs in `Weapon`, not `Player`. `Weapon` gets some methods to look after its own fields:
```java
int getDamage() {
	return damage;
}

void use() {
	if (durability > 0) {
		durability = durability - 1;
	}
}

boolean isBroken() {
	return durability == 0;
}
```
`use` also keeps durability from going below 0, the way `takeDamage` does for health.
`attack` now asks the weapon for what it needs:
```java
void attack(Player target) {
	if (isAlive()) {
		if (weapon == null) {
			target.takeDamage(1);
		} else {
			target.takeDamage(weapon.getDamage());
			weapon.use();
			if (weapon.isBroken()) {
				weapon = null;
			}
		}
	}
}
```
`attack` no longer knows or cares how weapons wear out. To make swords last longer than sticks, we'd only change `Weapon`.

## Public
The opposite of `private` is **`public`**, which means any code anywhere can use the thing. Our methods with it:
```java
public int getHealth() {
	return health;
}

public void takeDamage(int amount) {
	health = health - amount;
	if (health < 0) {
		health = 0;
	}
}
```
Constructors can be `public` too:
```java
public Player(int startingHealth) {
	health = startingHealth;
	maxHealth = startingHealth;
}
```
What were our methods before, with neither word in front? Java let code in the same folder use them. All our game code is in one folder, so that has worked as well as `public`. Robot code is spread over many files and folders, so it marks `public` on anything other parts of the code need.
Having no word in front is called **default** access.

You may also run into **`protected`**, which sits between default and `public`. Code in the same folder can use it, and so can child classes, even in other folders. We won't need it, but here are all four, from most private to most public:

| Word | Who can use it |
| ---- | -------------- |
| `private` | Only the class itself |
| (nothing) | The class and anything in the same folder |
| `protected` | The same as nothing, plus child classes in other folders |
| `public` | Everyone |

Robot code follows a simple rule of thumb:
- **Fields** are `private`.
- **Methods** that other classes need are `public`.
- **Methods** that only the class itself uses are `private` too. Methods can be private just like fields.

That explains another piece of the template from [Your First Program](ch01-01-first-program.md). `main` is `public` because Java itself calls it when your program starts, and Java's code isn't inside `Game`. That leaves `static`, which comes soon, and `String[]`.

### Two things to watch out for
> [!WARNING]
> **Overriding.** If `Player`'s `takeDamage` is `public`, `Tank`'s version has to be `public` too:
> ```java
> @Override
> public void takeDamage(int amount) {
> 	if (amount > 5) {
> 		super.takeDamage(amount - 5);
> 	}
> }
> ```
> Leave it off and you get a red squiggly line saying you `Cannot reduce the visibility of the inherited method from Player`. A `Tank` is a `Player`, and anyone can call `takeDamage` on a `Player`, so anyone has to be able to call it on a `Tank`.

> [!WARNING]
> **Public classes.** A class can be `public` too, like `public class Player`. A `public` class has to live in a file with exactly the same name, so `public class Player` must be in `Player.java`. If the names don't match, the red squiggly line says `The public type Player must be defined in its own file`. Each of our classes already has its own file, so all of them can be `public`, the way robot code does it. From now on, ours will be.

### Printing an object
What do you get if you print a whole `Player`?
```java
Player bob = new Player(100);
System.out.println(bob);
```
```
Player@1b6d3586
```
(The letters and numbers after the `@` will be different for you.) Not very useful, and where did it come from?
Every class in Java has a parent, even without `extends`. A class with no `extends` automatically extends a class named `Object`, so every object in Java is an `Object`. `Object` has a method called `toString`, which Java calls whenever it needs an object as text: in `println`, or when you join it to a `String` with `+`. `Object`'s version returns the class name followed by a code for that particular object.
You can override it. `toString` is `public` in `Object`, so our version has to be `public` too:
```java
@Override
public String toString() {
	return "Player with " + health + " health";
}
```
Now `System.out.println(bob);` prints:
```
Player with 100 health
```
Many WPILib classes override `toString`, so printing them gives you something readable. It also comes up in robot code when a command's name is built with `+`. `toString` is what turns each piece into text.

{{#quiz ../quizzes/ch01-12-tostring.toml}}

## Final
The constructor sets `maxHealth`, and nothing changes it afterward. That's intentional, since a `Player`'s max health should stay the same all game. **`final`** makes Java enforce it:
```java
private final int maxHealth;
```
`final` means "this gets set once and can never change." If a method tries to change it, like this one:
```java
void boostMaxHealth() {
	maxHealth = maxHealth + 10;
}
```
you get a red squiggly line saying `The final field Player.maxHealth cannot be assigned`.
A `final` field *must* be set, either in the constructor or right where the field is written. Forget, and the red squiggly line says it `may not have been initialized`.

`Weapon`'s `name` and `damage` never change either, so they can be `final`:
```java
private final String name;
private final int damage;
private int durability;
```
`durability` *does* change each time the weapon is used, so it can't be `final`.

### Final objects
When a `final` field holds an object, remember what the field really holds: the object's *address*. `final` means the field always holds the address of the same object. The object itself can still change. For example:
```java
public class Knight {
	private final Weapon sword = new Weapon("Sword", 25);

	public void swing() {
		sword.use();
	}

	public void upgrade() {
		sword = new Weapon("Big Sword", 50);
	}
}
```
(See the `= new Weapon(...)` right where the field is written? That's another way to give a field its starting value without a constructor, and robot code does it a lot.)
`swing` is fine. It doesn't change `sword`. It uses the weapon `sword` points to, and the weapon updates its own durability. `upgrade` gets a red squiggly line, because it tries to point `sword` at a *different* weapon.

{{#quiz ../quizzes/ch01-12-public-final.toml}}

## What this looks like on a robot
You can read real robot code now. Here's part of a class for a robot's arm. You don't need to run it, but read through it:
```java
public class Arm implements Mechanism {
	private final SparkMax motor = new SparkMax(CANPort.CAN_S0, 5, MotorType.kBrushless);
	private final RelativeEncoder encoder = motor.getEncoder();

	public void setSpeed(double speed) {
		if (speed > 0.5) {
			speed = 0.5;
		} else if (speed < -0.5) {
			speed = -0.5;
		}
		motor.setThrottle(speed);
	}

	public double getPosition() {
		return encoder.getPosition();
	}
}
```
- `public class Arm implements Mechanism` is a class in its own file, `Arm.java`. `implements Mechanism` tells WPILib that the arm is one of the robot's parts. It's a little like `extends`, and [Interfaces](ch01-15-interfaces.md) explains it properly.
- `motor` is a field holding a `SparkMax`, the class for one kind of motor controller. `CANPort.CAN_S0` and `5` pick out which motor on the robot it talks to: the CAN connector it's wired to, and its ID number. It's `final` because the arm uses the same motor forever. The motor can still change speed, the same way our knight's sword can still wear out.
- `motor` is `private`, so no other code can grab the arm's motor and spin it. `setSpeed` is the *only* way to move the arm.
- `setSpeed` keeps the speed between -0.5 and 0.5 before passing it to the motor with `setThrottle`. (A motor's throttle runs from -1, full speed backward, to 1, full speed forward.) It's the same pattern as `move`. And since `motor` is private, nothing can skip the limit, the way nothing can skip `takeDamage` now.
- `getPosition` is a getter, but it doesn't hand back a field. It asks the encoder how many times the motor has turned.

## Putting it together
Here's everything now, with private fields and public methods:
{{#listing ch01-12}}

## Try it yourself
Time to keep score. Give every `Player` a score that starts at 0. Whenever a `Player`'s attack knocks someone out (they were alive before the attack and aren't after it), the attacker gets 1 point.
Other code should be able to *see* a `Player`'s score, but nothing outside `Player` should be able to change it.

*Hint: to know whether an attack knocked someone out, you have to check whether they were alive before the attack. How can you hold on to that answer until the attack is over?*

To check your answer, make your changes and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-12/Game.java}}
```
If every line prints what its comment says, your score works. The third attack checks that hitting someone who's already knocked out doesn't count.
Then remove the `//` from the last line. If it gets a red squiggly line, nobody can cheat.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-12}}
`Player` gets a private `score` field and a getter for it, so other code can see the score but not change it. `attack` checks whether the target was alive *before* the attack and keeps the answer in a variable.
`wasAlive` isn't a field. It's a variable inside a method, like the variables you make in `main`. A new `wasAlive` is made each time `attack` runs, and it disappears when `attack` finishes. That's all we need, since the answer only has to last until the end of the attack.

</details>
