# Units

## Which degrees?
Our arm measures its angle in degrees, but not everything on a real robot does. REV's encoders measure in rotations. WPILib's simulator, which comes up soon, measures angles in radians, a different unit where a full circle is about 6.28 instead of 360. Distances might be in inches, meters or feet.

[Decimals and Math](ch01-14-decimals-and-math.md) put the unit in every constant's name, like `TOLERANCE_DEGREES`, to make mix-ups harder. Harder still isn't impossible, though. Nothing stops you from writing this:
```java
double target = 1.4;
arm.isAtRadians(target);
```
Is `target` in radians, degrees or rotations? A `double` is just a number, so Java can't tell. Guess wrong and the code still runs. It just sends the arm to the wrong place.

WPILib offers something better: instead of a plain `double`, you can store a measurement as an object that knows its own unit.

## Measurements with units
Here's an angle:
```java
Angle scoringAngle = Degrees.of(80);
```
`Angle` is a type for angles, and `Degrees.of(80)` makes one that's 80 degrees. You can then read it back in any unit you need:
```java
System.out.println(scoringAngle.in(Degrees));
System.out.println(scoringAngle.in(Radians));
System.out.println(scoringAngle.in(Rotations));
```
That prints `80.0`, then about `1.396`, then about `0.222`. It's one angle described in three units. WPILib does the conversion, so you never have to remember how many radians are in a degree.

Java also won't let you mix up *kinds* of measurement. `Degrees.of(80)` is an `Angle`, and `Meters.of(2)` is a `Distance`. Try to put a `Distance` in an `Angle` variable and you get a red squiggly line.

Some types and their units:

| Type              | Some of its units                              |
| ----------------- | ---------------------------------------------- |
| `Angle`           | `Degrees`, `Radians`, `Rotations`              |
| `Distance`        | `Meters`, `Inches`, `Feet`                     |
| `Time`            | `Seconds`, `Milliseconds`                      |
| `AngularVelocity` | `RotationsPerSecond`, `RPM`, `DegreesPerSecond` |
| `LinearVelocity`  | `MetersPerSecond`, `FeetPerSecond`             |

### Comparing
An `Angle` is an object, not a number, so `<` gets a red squiggly line. And `==` on two objects only checks whether they're the *same object*, as [Objects Working Together](ch01-09-objects-working-together.md) explained, not whether they're the same angle. An `Angle` has methods for comparing instead:
- `a.lt(b)` means "less than": is `a` smaller than `b`? There's also `gt` ("greater than"), `lte` ("less than or equal") and `gte` ("greater than or equal").
- `a.isNear(b, tolerance)` is the "close enough" check from [Decimals and Math](ch01-14-decimals-and-math.md): is `a` within `tolerance` of `b`?

The two angles don't even have to use the same unit. `Degrees.of(180).isNear(Rotations.of(0.5), Degrees.of(1))` is `true`, because half a rotation *is* 180 degrees.

### Importing units
Using `Degrees` takes an import of a slightly different kind:
```java
import static org.wpilib.units.Units.Degrees;
import org.wpilib.units.measure.Angle;
```
`Degrees` is a `static` field in a class called `Units`, so normally you'd have to write `Units.Degrees` every time. An **`import static`** lets you use a `static` field or method without the class name in front, so you can write plain `Degrees`. Each unit you use needs its own `import static`. (VS Code's Quick Fix can add them for you.)

{{#quiz ../quizzes/ch02-03-units.toml}}

## Units in our arm
Now the arm gets units. `ArmPosition` becomes:
```java
public enum ArmPosition {
	STOWED(Degrees.of(0)),
	INTAKE(Degrees.of(15)),
	SCORING(Degrees.of(80));

	private final Angle angle;

	ArmPosition(Angle positionAngle) {
		angle = positionAngle;
	}

	public Angle getAngle() {
		return angle;
	}
}
```
The tolerance in `ArmConstants` becomes an `Angle` as well. The type already says what it's measured in, so `DEGREES` can come out of the name:
```java
public static final Angle TOLERANCE = Degrees.of(2);
```
In `Arm`, `getAngleDegrees` becomes `getAngle`, and anything that compares angles uses the methods instead of `<`:
```java
public Angle getAngle() {
	return Degrees.of(io.getAngleDegrees());
}

public boolean isAt(ArmPosition position) {
	return getAngle().isNear(position.getAngle(), ArmConstants.TOLERANCE);
}

public void update() {
	io.update();
	Telemetry.log("Arm/Angle", getAngle().in(Degrees));
}
```
```java
public Command moveTo(ArmPosition position) {
	return run(coroutine -> {
		while (!isAt(position)) {
			if (getAngle().lt(position.getAngle())) {
				setThrottle(ArmConstants.MAX_THROTTLE);
			} else {
				setThrottle(-ArmConstants.MAX_THROTTLE);
			}
			coroutine.yield();
		}
		setThrottle(0);
	}).named("Arm.MoveTo[" + position + "]");
}
```
`ArmIO` still uses a `double` in degrees, and that's deliberate. The IO is where our code meets the hardware, and every motor library does things its own way, so `getAngle` turns the IO's number into an `Angle` immediately. Everywhere else in our code, an angle is an `Angle`, and its units can't get mixed up.
`MAX_THROTTLE` stays a `double` as well. A throttle is a fraction of full power, from -1 to 1, so it has no unit.

Run it in the simulator again. It should behave exactly as before, but now if anyone mixes up degrees and radians, Java will catch it.

{{#quiz ../quizzes/ch02-03-units-in-the-arm.toml}}

## Try it yourself
If the real arm ever jams, `moveTo` never reaches its position. It never finishes, and it keeps pushing the motor as hard as it's allowed. Give it a time limit: if the arm hasn't arrived after `MOVE_TIMEOUT`, give up. Make `MOVE_TIMEOUT` 3 seconds, in `ArmConstants`.

*Hint: `MOVE_TIMEOUT` is an amount of time, so what type should it be? Then look through the methods a `Command` has. Does one sound like it puts a time limit on a command?*

To test it in the simulator, temporarily set `MAX_THROTTLE` to something tiny like `0.01` so the arm crawls. Press the button for `SCORING`, and after 3 seconds the arm should stop by itself. (Remember to change `MAX_THROTTLE` back.)

<details>
<summary>Answer</summary>

Here's one way to do it. In `ArmConstants`:
```java
public static final Time MOVE_TIMEOUT = Seconds.of(3);
```
And in `Arm`, add `.withTimeout(...)` to the end of `moveTo`:
```java
public Command moveTo(ArmPosition position) {
	return run(coroutine -> {
		// ...the same as before
	}).named("Arm.MoveTo[" + position + "]").withTimeout(ArmConstants.MOVE_TIMEOUT);
}
```
`withTimeout` makes a new command that runs `moveTo`'s command and cancels it if it's still going after `MOVE_TIMEOUT`. `MOVE_TIMEOUT` is a `Time`, so there's no question of whether it means seconds or milliseconds.
When the timeout cancels `moveTo`, the `setThrottle(0)` after its loop never runs, since a canceled command just stops. The arm still stops right away, though. Nothing is using the arm anymore, so the scheduler runs `idle()`, which sets the throttle to 0.

</details>
