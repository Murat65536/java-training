# Decimals and Math

## Decimals
Every number in our game so far has been a whole number. Robot code is the opposite. Nearly every number in it is a decimal: a motor's speed, how far an arm has turned, which way the robot is facing, how many seconds are left in the match. [Variables](ch01-02-variables.md) showed that decimals go in a `double`. This lesson is about how they mix with whole numbers.

`Tank`s are getting better armor. Instead of blocking `TANK_ARMOR` damage, a `Tank` will take only 75% of the damage from each hit, so a 20 damage hit does 15. 75% is `0.75`, so `TANK_ARMOR` in `Constants` gets replaced with a new constant. It's a decimal, so it's a `double`:
```java
public static final double TANK_DAMAGE_MULTIPLIER = 0.75;
```
A first try at the multiplying:
```java
@Override
public void takeDamage(int amount) {
	super.takeDamage(amount * Constants.TANK_DAMAGE_MULTIPLIER);
}
```
That gets a red squiggly line: `The method takeDamage(int) in the type Player is not applicable for the arguments (double)`. Put simply, `takeDamage` needs an `int`, and it was given a `double`.
Why is `amount * Constants.TANK_DAMAGE_MULTIPLIER` a `double`? Printing a couple of examples shows it:
```java
System.out.println(20 * 0.75);
System.out.println(10 * 0.75);
```
```
15.0
7.5
```
The rule is: **if either side of the math is a `double`, the answer is a `double`.** `TANK_DAMAGE_MULTIPLIER` is a `double`, so `amount * Constants.TANK_DAMAGE_MULTIPLIER` is too, even when the answer happens to be a whole number like `15.0`. Java can't know ahead of time that the answer won't be something like `7.5`.

The other direction causes no trouble. An `int` can go into a `double`, and Java turns it into a decimal for you:
```java
double d = 5;
System.out.println(d);
```
```
5.0
```
Nothing is lost when `5` becomes `5.0`. Turning `7.5` into an `int` *would* lose something, and as you saw in [Variables](ch01-02-variables.md), Java won't do that unless you tell it to.

## Casting
To tell Java "turn this into an `int`, and I know the decimal part will be lost," put `(int)` in front of it:
```java
System.out.println((int) 7.5);
System.out.println((int) 7.9);
```
```
7
7
```
This is called **casting**. It doesn't round. It chops off everything after the `.`, so even `7.9` becomes `7`.

That's enough to fix `Tank`:
```java
@Override
public void takeDamage(int amount) {
	super.takeDamage((int) (amount * Constants.TANK_DAMAGE_MULTIPLIER));
}
```
```java
Tank tom = new Tank(100);
tom.takeDamage(20);
System.out.println("tom: " + tom.getHealth());
tom.takeDamage(3);
System.out.println("tom: " + tom.getHealth());
```
```
tom: 85
tom: 83
```
The 20 damage hit did 15. The 3 damage hit would have done 2.25, which got chopped to 2.

> [!WARNING]
> Watch the parentheses around `amount * Constants.TANK_DAMAGE_MULTIPLIER`. A cast only applies to whatever comes right after it. Without them, `(int) amount * Constants.TANK_DAMAGE_MULTIPLIER` would turn `amount` into an `int` (which it already is), then multiply by `0.75`, and you'd be back to a `double`.

To round instead of chopping, use `Math.round`. It returns a `long` (a really big whole number), so it still needs `(int)` in front: `(int) Math.round(7.5)` is `8`. `Math.floor` rounds a number down, and `Math.ceil` rounds it up. For our game, truncating (chopping off the decimal) is fine.

## Dividing
Next, a method that reports what percent of their health a `Player` has left. 70 out of 100 health is 70%, so we divide and then multiply by 100:
```java
public double getHealthPercent() {
	return health / maxHealth * 100;
}
```
```java
Player bob = new Player(100);
bob.takeDamage(30);
System.out.println(bob.getHealthPercent());
```
```
0.0
```
That's wrong. A simpler example shows why:
```java
System.out.println(7 / 2);
System.out.println(7.0 / 2);
```
```
3
3.5
```
Dividing an `int` by an `int` gives an `int`. There's no room for the `.5`, so it's chopped off, the same as a cast. This is called **integer division**. As soon as one side is a `double`, like `7.0`, you get the decimal answer.

In `getHealthPercent`, `health / maxHealth` is `70 / 100`. That's `0.7`, truncated to `0`, and `0 * 100` is `0`. The value only became a `double` at the very end, when `return` handed it back, and by then the `.7` was long gone.

The fix is to make one side a `double` *before* the division:
```java
return (double) health / maxHealth * 100;
```
`(double)` turns `health` into `70.0` before dividing, so the division gives `0.7` as intended. (Casting works with `double` as well as `int`.)
```
70.0
```

Integer division is a sneaky bug, because there's no red squiggly line and no crash. You just get the wrong number. Here's a real example from robot code. An arm's motor usually turns many times for each swing of the arm, through gears. If the motor turns 25 times for every 1 turn of the arm, someone might write this in `ArmConstants`:
```java
public static final double GEAR_RATIO = 1 / 25;
```
`1 / 25` is integer division, so it's `0`, and `GEAR_RATIO` is `0.0`. Every arm position calculated with it comes out as 0, wherever the arm really is. It should be `1.0 / 25`, which is `0.04`.
This mistake is common enough that WPILib checks for it. In a robot project, this line won't build, and you get an error saying `Integer division in a floating-point context.` The check only catches simple cases, though, where the division goes straight into a `double`. It misses something like `health / maxHealth * 100`, where the division is buried in more math. You still have to watch for it yourself.

> [!IMPORTANT]
> Whenever you divide, check whether both sides are `int`s.

{{#quiz ../quizzes/ch01-14-decimals.toml}}

## Close enough
Decimals have one more surprise:
```java
System.out.println(0.1 + 0.2);
System.out.println(0.1 + 0.2 == 0.3);
```
```
0.30000000000000004
false
```
Computers can't always store decimals exactly. It's like trying to write ⅓ as a decimal: `0.333333` is close, but however many 3s you add, it's never *exactly* ⅓. The difference is usually too tiny to matter, but it means `==` on two `double`s can say `false` when you'd expect `true`.

On a robot there's an even bigger reason. Say the arm has a `getAngle()` method that returns its angle in degrees. You're moving the arm to some angle and want a method that says when it's there:
```java
public boolean isAtAngle(double targetAngle) {
	return getAngle() == targetAngle;
}
```
If `targetAngle` is `45.0`, a real arm will be at 44.97 or 45.02, and almost never at *exactly* 45.0. So this would almost never return `true`, and any code waiting on the arm would wait forever.
The fix is to check whether it's *close enough*, meaning the gap between where the arm is and where it should be is small. How small is a rule about our robot, so it goes in `ArmConstants`:
```java
public static final double TOLERANCE_DEGREES = 0.5;
```
```java
public boolean isAtAngle(double targetAngle) {
	return Math.abs(getAngle() - targetAngle) < ArmConstants.TOLERANCE_DEGREES;
}
```
`getAngle() - targetAngle` is how far off the arm is. With a target of 45 and the arm at 44.8, that's `-0.2`, since it's below the target. At 45.3 it's `0.3`, since it's above. `Math.abs` removes the minus sign, so either way you get the size of the gap. Then `< ArmConstants.TOLERANCE_DEGREES` checks that it's within half a degree.
A number like `TOLERANCE_DEGREES` is called a **tolerance**. Its name also says what unit it's in. Robots use lots of units, like degrees, rotations, inches and meters, and mixing them up is easy. A unit in the name makes that mistake easier to spot.

> [!IMPORTANT]
> Never use `==` to compare `double`s. Check whether they're close enough instead.

## More math
A few more things from `Math` that come up in robot code:
- `Math.PI` is a `static final` constant, like ours: `3.141592653589793`.
- `Math.sqrt(x)` returns the square root of `x`.
- `Math.round(x)` rounds `x`, as shown above.

For example, each time a wheel turns once, the robot rolls the distance around the wheel, which is `Math.PI` times the wheel's diameter. Say our robot has 4-inch wheels, so `DriveConstants` has:
```java
public static final double WHEEL_DIAMETER_INCHES = 4;
```
If the wheels have turned 3 times:
```java
double wheelRotations = 3;
double distance = wheelRotations * Math.PI * DriveConstants.WHEEL_DIAMETER_INCHES;
System.out.println(distance);
```
```
37.69911184307752
```
The robot has rolled about 37.7 inches.

### The arm, with decimals
Here's the arm's `setSpeed` written the way real robot code would write it:
```java
public void setSpeed(double speed) {
	motor.setThrottle(Math.max(-ArmConstants.MAX_SPEED, Math.min(speed, ArmConstants.MAX_SPEED)));
}
```
It's the same `max` around a `min` from `move`, with `double`s. Ask for a speed of `0.8` and `Math.min(0.8, 0.5)` makes it `0.5`. Ask for `-0.8` and `Math.max(-0.5, -0.8)` makes it `-0.5`. Anything in between goes straight to the motor.
(Newer versions of Java, including the one WPILib uses, also have a `static` method that does this in one step: `Math.clamp(speed, -ArmConstants.MAX_SPEED, ArmConstants.MAX_SPEED)`. "Clamp" is the usual name for keeping a number between two limits.)

{{#quiz ../quizzes/ch01-14-close-enough.toml}}

## Putting it together
Here's everything now. Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-14}}

## Try it yourself
Weapons should weaken as they wear out. Change `getDamage` so a weapon does less damage the less durability it has left:
- With all its durability left (3 out of 3), it does full damage.
- With 2 out of 3 left, it does ⅔ of its damage.
- With 1 out of 3 left, it does ⅓ of its damage.

A sword that does 25 damage would do 25, then 16, then 8, with the decimals chopped off.

*Hint: what fraction of its durability does a weapon have left? Don't write `3` for full durability, though, since that number already has a name. And watch out for the bug from the [Dividing](#dividing) section.*

To check your answer, make your changes and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-14/Game.java}}
```
If every line prints what its comment says, your weapons wear down correctly. (The last attack is a punch, because the sword broke.)
If `alice` stays at 75 after the second and third attacks, your weapon is doing 0 damage. Look for a division with an `int` on both sides.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-14}}
`durabilityLeft` is the fraction of its durability the weapon has left: `1.0` when new, then about `0.67`, then about `0.33`. The `(double)` is what makes it work. Without it, `durability / Constants.WEAPON_DURABILITY` would be integer division, giving `1` for a brand new weapon and `0` for every hit after that.

</details>
