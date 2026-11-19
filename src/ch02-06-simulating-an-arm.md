# Simulating an Arm

Our `SimArmIO` is a poor imitation of an arm. It moves the same amount every update, stops the instant the throttle hits 0, and has no gravity. A real arm speeds up and slows down, can keep moving for a moment after the motor stops, and falls if nothing holds it up. Code that works on our pretend arm might fail on the real one.
WPILib has a far better pretend arm called `SingleJointedArmSim`. It uses physics to work out what a real arm would do, based on how heavy it is, how strong the motor is and how hard gravity pulls.

## A real pretend arm
`SingleJointedArmSim` needs a description of the arm, which goes in `ArmConstants`. Here's the whole file now:
```java
package frc.robot.constants;

import static org.wpilib.units.Units.Degrees;
import static org.wpilib.units.Units.Kilograms;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Seconds;

import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.Mass;
import org.wpilib.units.measure.Time;

public final class ArmConstants {
	public static final double MAX_THROTTLE = 0.5;
	public static final Angle TOLERANCE = Degrees.of(2);
	public static final Time MOVE_TIMEOUT = Seconds.of(3);
	public static final double SLOW_MODE_TRIGGER_THRESHOLD = 0.5;
	public static final double SLOW_MODE_MULTIPLIER = 0.25;

	public static final double GEAR_RATIO = 200;
	public static final Distance LENGTH = Meters.of(0.6);
	public static final Mass MASS = Kilograms.of(4);
	public static final Angle MIN_ANGLE = Degrees.of(0);
	public static final Angle MAX_ANGLE = Degrees.of(100);

	public static final double DRAWING_SIZE = 2.0;
	public static final double DRAWING_PIVOT = 1.0;
}
```
`SIM_DEGREES_PER_UPDATE_AT_FULL_THROTTLE` is gone, since the new pretend arm works out its own movement. The new constants say:
- The motor turns 200 times for every 1 turn of the arm.
- The arm is 0.6 meters long and weighs 4 kilograms.
- It can't go below 0 degrees, which is level with the ground, or above 100 degrees. A real robot would have hard stops at those angles: pieces of metal the arm can't move past.

`DRAWING_SIZE` and `DRAWING_PIVOT` come into play a little later.

Here's the new `SimArmIO`:
```java
package frc.robot.mechanisms;

import static org.wpilib.units.Units.Degrees;
import static org.wpilib.units.Units.Kilograms;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Radians;

import frc.robot.constants.ArmConstants;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.math.system.DCMotor;
import org.wpilib.simulation.SingleJointedArmSim;
import org.wpilib.system.RobotController;

public class SimArmIO implements ArmIO {
	private final SingleJointedArmSim armSim = new SingleJointedArmSim(
			DCMotor.getNEO(1),
			ArmConstants.GEAR_RATIO,
			SingleJointedArmSim.estimateMOI(ArmConstants.LENGTH.in(Meters), ArmConstants.MASS.in(Kilograms)),
			ArmConstants.LENGTH.in(Meters),
			ArmConstants.MIN_ANGLE.in(Radians),
			ArmConstants.MAX_ANGLE.in(Radians),
			true,
			ArmConstants.MIN_ANGLE.in(Radians));
	private double throttle;

	@Override
	public double getAngleDegrees() {
		return Radians.of(armSim.getAngle()).in(Degrees);
	}

	@Override
	public void setThrottle(double newThrottle) {
		throttle = newThrottle;
	}

	@Override
	public void update() {
		armSim.setInputVoltage(throttle * RobotController.getBatteryVoltage());
		armSim.update(OpModeRobot.DEFAULT_PERIOD);
	}
}
```
That's a big constructor call. Its arguments, in order:
1. `DCMotor.getNEO(1)` says the arm is driven by 1 NEO, a motor made by REV. WPILib knows the strength and speed of every common FRC motor.
2. The gear ratio.
3. The arm's **moment of inertia**, meaning how hard it is to get the arm spinning. `estimateMOI` works it out from the arm's length and weight.
4. The arm's length.
5. The lowest and highest angles it can reach.
6. `true`, meaning "simulate gravity."
7. The angle the arm starts at.

`SingleJointedArmSim` takes plain numbers in meters, kilograms and radians, which is why every constant has a `.in(...)`. Units earn their keep in places like this: each line shows exactly which unit goes in.

The physics happens in `update`:
- A motor doesn't really take a throttle. It takes a **voltage**, which is how hard the battery pushes electricity through it. A throttle of 0.5 means "half of whatever the battery has," so we multiply by the battery's voltage.
- `armSim.update(OpModeRobot.DEFAULT_PERIOD)` moves the pretend arm forward in time by one update, 20 milliseconds, and works out where the arm would be by then. `DEFAULT_PERIOD` is a WPILib constant, so we don't need our own `0.02`.

`getAngleDegrees` converts the simulator's radians back to degrees, the way `RealArmIO` converts a real encoder's rotations. Either way, `Arm` gets degrees and can't tell the difference.

Look at what *didn't* change. `Arm`, every command and every opmode are exactly the same, because they only ever talk to an `ArmIO`.

{{#quiz ../quizzes/ch02-06-physics.toml}}

## Drawing the arm
A graph of the angle is useful, but it's much easier to understand the arm if you can *watch* it. WPILib can draw a simple picture of a mechanism, called a `Mechanism2d`. It's made of straight lines called **ligaments**, attached to a point called the **root**.
The drawing goes in `Arm`, not `SimArmIO`. That way it follows whatever the arm's angle says, so it works on the real robot too. Add these fields to `Arm`:
```java
private final Mechanism2d drawing = new Mechanism2d(ArmConstants.DRAWING_SIZE, ArmConstants.DRAWING_SIZE);
private final MechanismLigament2d armDrawing = drawing
		.getRoot("Pivot", ArmConstants.DRAWING_PIVOT, ArmConstants.DRAWING_PIVOT)
		.append(new MechanismLigament2d("Arm", ArmConstants.LENGTH.in(Meters), 0));
```
- `new Mechanism2d(...)` makes a drawing `DRAWING_SIZE` wide and tall.
- `getRoot("Pivot", ...)` puts the root, where the arm attaches, in the middle of the drawing.
- `append(...)` attaches a ligament to the root: a line named `"Arm"`, as long as the arm, starting at 0 degrees.

(`Arm` needs imports for `Mechanism2d` and `MechanismLigament2d`, which live in `org.wpilib.smartdashboard`, plus an `import static` for `Meters`.)

Then, in `update`, point the drawing at the arm's angle and send it to the dashboard:
```java
public void update() {
	io.update();
	armDrawing.setAngle(getAngle().in(Degrees));
	Telemetry.log("Arm/Angle", getAngle().in(Degrees));
	Telemetry.log("Arm/Drawing", drawing);
}
```

### Watching it
Start the simulator and connect AdvantageScope as before. Make a new tab for the drawing by clicking the **+** at the top of AdvantageScope and choosing **Mechanism**. Find **Arm**, then **Drawing**, in the list on the left, and drag it into the new tab.
Enable the robot in **Teleoperated** and press the buttons. The arm swings up to the scoring position and back down. Try the left stick too. The arm now moves at speeds a real arm with this motor and gearing could manage.

Now let go of everything with the arm partway up and watch closely. It slowly sinks. That's gravity. The old pretend arm could never have shown this, but a real arm would do exactly the same.

{{#quiz ../quizzes/ch02-06-drawing.toml}}

## Try it yourself
Stop the arm from sinking. To hold still, the motor has to push up just hard enough to cancel gravity. Gravity pulls hardest when the arm sticks straight out, level with the ground, and not at all when the arm points straight up. So the throttle needed to hold the arm still is:
```
GRAVITY_THROTTLE × cos(angle)
```
`GRAVITY_THROTTLE` is the throttle that holds the arm level. `cos` (short for "cosine") is a math function that's 1 at 0 degrees and shrinks as the angle rises, down to 0 at 90 degrees. In Java it's `Math.cos`, and it takes the angle in radians.
1. Add a `private double gravityThrottle()` method to `Arm` that works out the throttle above.
2. Add it to the throttle in `idle()` and `manualControl`, so the arm holds itself up whenever the driver isn't moving it.
3. Work out what `GRAVITY_THROTTLE` should be and put it in `ArmConstants`.

Step 3 takes experimenting, the same as it would on a real robot. Start with something tiny like `0.005`. Move the arm to the intake position, let go and watch. If it still sinks, raise the number a little. If it creeps *up*, the number is too big. Keep adjusting until the arm stays where you leave it.

<details>
<summary>Answer</summary>

Here's one way to do it. In `Arm`:
```java
private double gravityThrottle() {
	return ArmConstants.GRAVITY_THROTTLE * Math.cos(getAngle().in(Radians));
}

@Override
public Command idle() {
	return runRepeatedly(() -> setThrottle(gravityThrottle()))
			.withPriority(Command.LOWEST_PRIORITY)
			.named("Arm.Idle");
}

public Command manualControl(DoubleSupplier throttle) {
	return runRepeatedly(() -> setThrottle(throttle.getAsDouble() * ArmConstants.MAX_THROTTLE + gravityThrottle()))
			.withPriority(Command.LOWEST_PRIORITY)
			.named("Arm.ManualControl");
}
```
And in `ArmConstants`, something close to:
```java
public static final double GRAVITY_THROTTLE = 0.023;
```
Your number can be a little different, as long as the arm holds still. For this arm's motor, gear ratio, length and weight, physics puts the answer at about `0.0226`.
Units help again in `getAngle().in(Radians)`. `Math.cos` needs radians, and the code shows plainly that radians are what it gets.
Real robot code does the same thing under a fancier name. Holding an arm up against gravity is called **gravity feedforward**. On a real robot, trying values and adjusting until the arm holds still, as you just did, is one common way to find the number.

</details>
