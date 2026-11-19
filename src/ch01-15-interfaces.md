# Interfaces

## Things that aren't players
The arena needs some crates for players to smash. A crate has health and can take damage, and that's all it does. Here's its constant, and the class, which goes in a new file `Crate.java`:
```java
public static final int CRATE_HEALTH = 40;
```
```java
public class Crate {
	private int health;

	public Crate() {
		health = Constants.CRATE_HEALTH;
	}

	public void takeDamage(int amount) {
		health = Math.max(health - amount, 0);
	}

	public int getHealth() {
		return health;
	}
}
```
Smashing one:
```java
Player bob = new Player(100);
Crate crate = new Crate();
bob.attack(crate);
```
That gets a red squiggly line, because `attack` takes a `Player`, and a `Crate` isn't one.
Writing `class Crate extends Player` would make the error go away. But then a `Crate` *would* be a `Player`, with everything a `Player` has. Crates could `move` around the arena, `heal` themselves, even `attack` you. A crate isn't a kind of player, so `extends` is the wrong tool.

`attack` doesn't need a `Player`. It needs *something that can take damage*. Inside `attack`, the only things it does to `target` are call `takeDamage` and ask `isAlive()`. What we want is a way to say "`attack` works on anything that can take damage and tell us how much health it has left."

## Interfaces
An **interface** is a list of methods that a class promises to have. Here's one for things that can take damage. Like a class, it gets its own file, `Damageable.java`:
```java
public interface Damageable {
	void takeDamage(int amount);

	int getHealth();
}
```
It looks like a class, except it says `interface` instead of `class`, and its methods have no `{ }`. Each one is only the first line of a method with a `;` at the end. An interface doesn't say *how* to take damage. It says that anything `Damageable` *can*.

A class makes the promise with **`implements`**:
```java
public class Crate implements Damageable {
	// ...

	@Override
	public void takeDamage(int amount) {
		health = Math.max(health - amount, 0);
	}

	@Override
	public int getHealth() {
		return health;
	}
}
```
```java
public class Player implements Damageable {
	// ...everything else stays the same

	@Override
	public int getHealth() {
		return health;
	}

	@Override
	public void takeDamage(int amount) {
		health = Math.max(health - amount, 0);
	}
}
```

| `class Crate`    | `implements Damageable`                                     |
| ---------------- | ----------------------------------------------------------- |
| A new class | Promises to have every method that `Damageable` lists |

`Player` already had both methods, so it only needed `implements Damageable` and an `@Override` on each. `@Override` works with interfaces the same way it does with parent classes. It asks Java to check that the method really is one from the interface.

### Keeping the promise
Java holds every class to its promise. If `Crate` said `implements Damageable` but had no `getHealth`, `class Crate` would get a red squiggly line saying `The type Crate must implement the inherited abstract method Damageable.getHealth()`. ("Abstract" means a method with no `{ }`, like the ones in an interface.)

Everything in an interface is automatically `public`, which is why `Damageable` doesn't say `public` anywhere. An interface is a promise to every piece of code that uses it, so hiding part of it would make no sense. It also means the methods that keep the promise must be `public`. If `Crate`'s `getHealth` left out `public`, you'd get the same `Cannot reduce the visibility` red squiggly line from [Protecting Your Fields](ch01-12-protecting-your-fields.md).

## Using an interface as a type
An interface is a type, the same as a class. So `attack` can take a `Damageable` instead of a `Player`:
```java
public void attack(Damageable target) {
	boolean wasAlive = target.getHealth() > 0;
	if (isAlive()) {
		if (weapon == null) {
			target.takeDamage(Constants.PUNCH_DAMAGE);
		} else {
			target.takeDamage(weapon.getDamage());
			weapon.use();
			if (weapon.isBroken()) {
				weapon = null;
			}
		}
	}
	if (wasAlive && target.getHealth() == 0) {
		score = score + 1;
	}
}
```
The one other change is how it checks the target. It used to ask `target.isAlive()`, but that's a `Player` method and not every `Damageable` has it. Every `Damageable` does have `getHealth`, so it asks whether the health is above 0 instead.

`attack` now works on players *and* crates:
```java
Player bob = new Player(100);
Crate crate = new Crate();
bob.equip(new Weapon("Sword", 25));
bob.attack(crate);
System.out.println("crate: " + crate.getHealth());
bob.attack(crate);
System.out.println("crate: " + crate.getHealth());
System.out.println("score: " + bob.getScore());
```
```
crate: 15
crate: 0
score: 1
```
The sword did 25 damage, then 16 as it wore down, and the crate broke. Smashing a crate even counts as a point.
Players still work, `Tank`s included. `Tank` overrides `takeDamage`, and Java always runs the version that belongs to the actual object, as in [Inheritance](ch01-11-inheritance.md).

### What a Damageable can do
A variable's type can be an interface too:
```java
Damageable thing = new Crate();
thing.takeDamage(20);
System.out.println(thing.getHealth());
```
```
20
```
`thing` can hold *any* `Damageable`: a `Crate`, a `Player`, a `Tank`, anything that implements `Damageable`. The flip side is that Java only lets you use the methods `Damageable` promises, even when `thing` holds a `Player`:
```java
Damageable thing = new Player(100);
thing.move(5);
```
That gets a red squiggly line saying `The method move(int) is undefined for the type Damageable`. Some `Damageable`s can't move, so Java won't let you try. Inside `attack`, this is what stops the code from doing something to `target` that a crate can't do.

An interface has no code in its methods, so you can't make one on its own. `new Damageable()` gets a red squiggly line saying `Cannot instantiate the type Damageable`. You can only make objects of classes that implement it, like `new Crate()`.

> [!WARNING]
> When you change a method's parameters, like going from `attack(Player target)` to `attack(Damageable target)`, check the classes that override it. `Berserker`'s `attack` still says `attack(Player target)`, so it no longer replaces `Player`'s `attack`. It's now a separate method that happens to share the name. Since `Berserker`'s `attack` has `@Override`, you get a red squiggly line saying it `must override or implement a supertype method`. Change it to `attack(Damageable target)` too. Catching this kind of mistake is the whole reason `@Override` exists.

{{#quiz ../quizzes/ch01-15-interfaces.toml}}

## More than one interface
`extends` and `implements` differ in one more big way. A class can extend only *one* class, so `class Healer extends Player, Tank` gets a red squiggly line. A class can implement as many interfaces as it likes, separated by commas:
```java
public class Player implements Damageable, Healable {
```
That would promise a `Player` has every method in `Damageable` *and* every method in `Healable`. A class can also do both at once, like `class Tank extends Player implements Saveable`. The `extends` always comes first.

## Interfaces on a robot
Robot code uses interfaces in a particular way. Take our arm. Its code talks to a real motor and reads a real encoder. But you'll want to test the arm's code without a robot, in a simulator on your laptop, and there's no real motor there.
An interface solves this. It lists what the arm needs to do with its hardware:
```java
public interface ArmIO {
	double getAngle();

	void setSpeed(double speed);
}
```
(IO stands for "input/output": information coming in from the hardware, and commands going out to it.) One class does those things with the real hardware:
```java
public class RealArmIO implements ArmIO {
	private final SparkMax motor = new SparkMax(CANPort.CAN_S0, ArmConstants.MOTOR_ID, MotorType.kBrushless);
	private final RelativeEncoder encoder = motor.getEncoder();

	@Override
	public double getAngle() {
		return encoder.getPosition() * ArmConstants.DEGREES_PER_MOTOR_ROTATION;
	}

	@Override
	public void setSpeed(double speed) {
		motor.setThrottle(speed);
	}
}
```
Another does them with a pretend arm, which only moves a number up and down:
```java
public class SimArmIO implements ArmIO {
	private double angle;

	@Override
	public double getAngle() {
		return angle;
	}

	@Override
	public void setSpeed(double speed) {
		angle = angle + speed * ArmConstants.SIM_DEGREES_PER_UPDATE;
	}
}
```
The arm holds an `ArmIO` and neither knows nor cares which one:
```java
public class Arm implements Mechanism {
	private final ArmIO io;

	public Arm(ArmIO armIO) {
		io = armIO;
	}

	public void setSpeed(double speed) {
		io.setSpeed(Math.max(-ArmConstants.MAX_SPEED, Math.min(speed, ArmConstants.MAX_SPEED)));
	}

	public double getAngle() {
		return io.getAngle();
	}
}
```
That `implements Mechanism` first appeared in [Protecting Your Fields](ch01-12-protecting-your-fields.md), and now it makes sense. `Mechanism` is a WPILib interface that every part of the robot implements. What it promises comes up once we start writing robot code.

On the real robot, the code makes `new Arm(new RealArmIO())`. In the simulator, it makes `new Arm(new SimArmIO())`. Everything in `Arm`, like the speed limit, is the same code both times, so what you test on your laptop is what runs on the robot. It's the same idea as `attack`. `attack` doesn't care whether its target is a player or a crate, and `Arm` doesn't care whether its motor is real or pretend.

{{#quiz ../quizzes/ch01-15-using-interfaces.toml}}

## Putting it together
Here's everything now. Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-15}}

## Try it yourself
Give players special abilities:
1. Make an interface named `Ability` with one method, `void use(Player user, Damageable target)`. `user` is the `Player` using the ability, and `target` is what they use it on.
2. A `Player` can `learn` an ability with `public void learn(Ability newAbility)`. A `Player` knows one ability at a time, so learning a new one replaces the old one.
3. A `Player` can use their ability on something with `public void useAbility(Damageable target)`. If the `Player` is knocked out or hasn't learned an ability, nothing happens.
4. A `Fireball` ability does `FIREBALL_DAMAGE` damage to the target. Make `FIREBALL_DAMAGE` 30.
5. A `Drain` ability does `DRAIN_AMOUNT` damage to the target *and* heals the user by `DRAIN_AMOUNT`. Make `DRAIN_AMOUNT` 10.

*Hint: inside `useAbility`, the ability needs to know who's using it. Who is that? Think back to `this` in [Reading the error](ch01-10-objects-inside-objects.md#reading-the-error).*

To check your answer, make your changes and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-15/Game.java}}
```
If every line prints what its comment says, your abilities work.
A whole class for every ability is a lot of typing, though, especially for one that's a single line. The next lesson shows a much shorter way.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-15}}
`Fireball` and `Drain` each implement `Ability`, and `Player` holds whichever one it learned last. `useAbility` checks that there's an ability to use, so a `Player` who never learned one doesn't crash with a `NullPointerException`.
The tricky part is `ability.use(this, target)`. The ability needs to know which `Player` is using it, and `this` means "the object this method was called on." So when `bob.useAbility(alice)` runs, `this` is `bob`. The next lesson starts with more about `this`.

</details>
