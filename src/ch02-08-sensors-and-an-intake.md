# Sensors and an Intake

An arm by itself can't do much. To score, a robot has to pick up game pieces, hold on to them, and let go at the right moment. That's the job of an **intake**, usually a set of spinning wheels called **rollers** that pull a game piece in or push it back out.
Our arm is getting an intake on its end. Along the way comes a new kind of command, one that keeps going until a *sensor* says it's done.

## Sensors
A **sensor** is anything that lets the robot measure the world. You've used one already: the arm's encoder, which measures its angle. A few more show up on nearly every robot:
- A **limit switch** is a button a mechanism presses when it reaches the end of its travel. It's either pressed or not.
- A **beam break** shines a beam of light across a gap to a sensor on the other side. When something gets in the way, like a game piece, the beam is "broken." It's either broken or not.
- A **gyro** measures which way the robot is facing. [Driving](ch02-09-driving.md) needs one.

Limit switches and beam breaks are **digital** sensors, meaning they only ever say `true` or `false`. In WPILib they're a `DigitalInput`, plugged into one of the robot controller's numbered ports, and its `get()` method returns a `boolean`.
Our intake has a beam break right where a game piece sits once it's fully pulled in. If the beam is broken, the intake has a game piece.

## The intake's IO
The intake gets an IO interface, like the arm. Here's `mechanisms/IntakeIO.java`:
```java
package frc.robot.mechanisms;

public interface IntakeIO {
	boolean hasGamePiece();

	void setThrottle(double throttle);

	void update();
}
```
The rollers use a throttle, not a voltage. Unlike the arm, nobody minds if the rollers spin a touch slower at the end of a match, as long as they still pull the game piece in.

Here's what `RealIntakeIO` would look like on the real robot (as with `RealArmIO`, don't add it to your project):
```java
public class RealIntakeIO implements IntakeIO {
	private final SparkMax motor = new SparkMax(CANPort.CAN_S0, IntakeConstants.MOTOR_ID, MotorType.kBrushless);
	private final DigitalInput beamBreak = new DigitalInput(IntakeConstants.SENSOR_CHANNEL);

	@Override
	public boolean hasGamePiece() {
		return !beamBreak.get();
	}

	@Override
	public void setThrottle(double throttle) {
		motor.setThrottle(throttle);
	}

	@Override
	public void update() {}
}
```
Note the `!` in `hasGamePiece`. Many beam breaks say `true` when the light gets through and `false` when something blocks it, so "we have a game piece" means "*not* seeing the light." Every sensor is a little different, and the way to learn how yours behaves is to test it: put a game piece in and see what it says. Details like this belong in the IO, so the rest of the code can simply ask `hasGamePiece()`.

The simulator has no game pieces, so `SimIntakeIO` has to fake them. It pretends a game piece is always waiting, so once the rollers have pulled inward long enough, it has one. When the rollers spin outward, the game piece is gone. Make `constants/IntakeConstants.java`:
```java
package frc.robot.constants;

import static org.wpilib.units.Units.Seconds;

import org.wpilib.units.measure.Time;

public final class IntakeConstants {
	public static final double INTAKE_THROTTLE = 0.8;
	public static final double EJECT_THROTTLE = -0.5;
	public static final Time EJECT_TIME = Seconds.of(0.5);
	public static final Time SIM_PICKUP_TIME = Seconds.of(1);
}
```
And `mechanisms/SimIntakeIO.java`:
```java
package frc.robot.mechanisms;

import static org.wpilib.units.Units.Seconds;

import frc.robot.constants.IntakeConstants;
import org.wpilib.framework.OpModeRobot;

public class SimIntakeIO implements IntakeIO {
	private double throttle;
	private double secondsIntaking;
	private boolean hasGamePiece = true;

	@Override
	public boolean hasGamePiece() {
		return hasGamePiece;
	}

	@Override
	public void setThrottle(double newThrottle) {
		throttle = newThrottle;
	}

	@Override
	public void update() {
		if (throttle > 0) {
			secondsIntaking = secondsIntaking + OpModeRobot.DEFAULT_PERIOD;
		} else {
			secondsIntaking = 0;
		}

		if (secondsIntaking >= IntakeConstants.SIM_PICKUP_TIME.in(Seconds)) {
			hasGamePiece = true;
		} else if (throttle < 0) {
			hasGamePiece = false;
		}
	}
}
```
- On each update while the rollers pull in, `secondsIntaking` grows by one update's worth of time, 0.02 seconds. When they stop, it resets to 0.
- After the rollers have pulled in for `SIM_PICKUP_TIME`, it has a game piece. While they spin outward, it doesn't.
- `hasGamePiece` starts out `true`. In most FRC games, each robot starts the match already holding a game piece, called a **preload**, so it can score in autonomous.

## The intake
Now `mechanisms/Intake.java`:
```java
package frc.robot.mechanisms;

import frc.robot.constants.IntakeConstants;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.telemetry.Telemetry;

public class Intake implements Mechanism {
	private final IntakeIO io;

	public Intake(IntakeIO intakeIO) {
		io = intakeIO;
	}

	public boolean hasGamePiece() {
		return io.hasGamePiece();
	}

	public void update() {
		io.update();
		Telemetry.log("Intake/HasGamePiece", hasGamePiece());
	}

	public Command intake() {
		return run(coroutine -> {
			io.setThrottle(IntakeConstants.INTAKE_THROTTLE);
			coroutine.waitUntil(this::hasGamePiece);
			io.setThrottle(0);
		}).named("Intake.Intake");
	}

	public Command eject() {
		return run(coroutine -> {
			io.setThrottle(IntakeConstants.EJECT_THROTTLE);
			coroutine.wait(IntakeConstants.EJECT_TIME);
			io.setThrottle(0);
		}).named("Intake.Eject");
	}

	@Override
	public Command idle() {
		return runRepeatedly(() -> io.setThrottle(0))
				.withPriority(Command.LOWEST_PRIORITY)
				.named("Intake.Idle");
	}
}
```
`intake()` is the new kind of command. It starts the rollers, then `coroutine.waitUntil(this::hasGamePiece)` waits until the sensor reports a game piece. It yields while it waits, so the rest of the robot keeps running, like `coroutine.wait` in [Autonomous](ch02-05-autonomous.md). Then it stops the rollers, and the command is finished.
It's close to the `while` loop in the arm's `moveTo`. `waitUntil` is a shortcut for a loop that does nothing but wait:
```java
while (!hasGamePiece()) {
	coroutine.yield();
}
```
`eject()` has no sensor to wait on. When the beam break stops seeing the game piece, the piece may not be all the way out yet. So `eject()` spins the rollers outward for `EJECT_TIME`, long enough to push any game piece clear.

{{#quiz ../quizzes/ch02-08-intake.toml}}

## Hooking it up
In `Robot.java`, add the intake next to the arm and update it in `robotPeriodic()`:
```java
final Arm arm = new Arm(new SimArmIO());
final Intake intake = new Intake(new SimIntakeIO());
```
```java
@Override
public void robotPeriodic() {
	arm.update();
	intake.update();
	Scheduler.getDefault().run();
}
```
In `DriverTeleop`, give the driver two more controls, the left face button to intake and the left trigger to eject:
```java
robot.driverController.faceLeft().onTrue(robot.intake.intake());
robot.driverController.leftTrigger().onTrue(robot.intake.eject());
```
`leftTrigger()` is a `Trigger` that's true while the left trigger is squeezed more than halfway. (There's a `rightTrigger()` as well.)

Try it in the simulator with **Intake/HasGamePiece** on AdvantageScope's graph. It starts `true` because of the preload. Squeeze the left trigger and it turns `false`. Press the left face button, and about a second later it turns `true` again and the rollers stop by themselves.

## Sensors as triggers
A sensor that reports `true` or `false` fits naturally into a `Trigger`. Add this field to `Intake`:
```java
public final Trigger holdingGamePiece = new Trigger(this::hasGamePiece);
```
(It needs `import org.wpilib.command3.Trigger;`.) Now other code can bind commands to the intake's sensor the same way it binds them to buttons. Triggers can also be combined with `and`, from [Controllers and Teleop](ch02-04-controllers-and-teleop.md), which makes the buttons smarter. There's no reason to intake when we already have a game piece, or to eject when we don't:
```java
robot.driverController.faceLeft()
		.and(robot.intake.holdingGamePiece.negate())
		.onTrue(robot.intake.intake());
robot.driverController.leftTrigger()
		.and(robot.intake.holdingGamePiece)
		.onTrue(robot.intake.eject());
```
`negate()` flips a trigger, so it's true whenever `holdingGamePiece` is false. The left face button now intakes only when the intake is empty, and the left trigger ejects only when it's holding something.

## Mechanisms working together
Picking up a game piece takes two mechanisms. The arm goes down to the intake position, the rollers pull the game piece in, and then the arm comes back up so the game piece doesn't drag on the ground. One command can do all of that.
It doesn't belong in `Arm` or `Intake`, since it uses both. Commands that use more than one mechanism go in `Robot`. Add this method to `Robot`:
```java
Command pickUp() {
	return Command.noRequirements(coroutine -> {
		coroutine.awaitAll(arm.moveTo(ArmPosition.INTAKE), intake.intake());
		coroutine.await(arm.moveTo(ArmPosition.STOWED));
	}).named("Robot.PickUp");
}
```
(`Robot` needs imports for `Command` and `ArmPosition`.) It lowers the arm and starts the rollers at the same time, using `awaitAll` from [Autonomous](ch02-05-autonomous.md). Once the arm is down *and* the intake has a game piece, it stows the arm.
In `DriverTeleop`, change the left face button to use it:
```java
robot.driverController.faceLeft()
		.and(robot.intake.holdingGamePiece.negate())
		.onTrue(robot.pickUp());
```
Try it. A single button press now does the whole pickup.

Suppose the driver changes their mind halfway and presses the scoring button while `pickUp` is still lowering the arm. `moveTo(ArmPosition.SCORING)` needs the arm, and `pickUp` is using it at that moment, so the scheduler cancels the *whole* `pickUp` command, including the intake command inside it. Then the arm heads for the scoring position. The driver always gets the last thing they asked for.

{{#quiz ../quizzes/ch02-08-together.toml}}

## Try it yourself
The robot finally has a game piece to score, so `ScoreAndStowAuto` should actually score it. Change it so that once the arm reaches the scoring position, it ejects the game piece instead of just waiting. Then it should stow the arm as before.

*Hint: the intake already has a command that ejects a game piece and then finishes. How does the routine wait for a command to finish?*

Once you're done, `AutoConstants.SCORE_TIME` isn't used anymore, so you can delete it. To test, run **Score and Stow** in autonomous with **Intake/HasGamePiece** on the graph. It should start `true` because of the preload and turn `false` once the arm reaches the top.

<details>
<summary>Answer</summary>

Here's one way to do it:
```java
private Command scoreAndStow(Robot robot) {
	return Command.noRequirements(coroutine -> {
		coroutine.await(robot.arm.moveTo(ArmPosition.SCORING));
		coroutine.await(robot.intake.eject());
		coroutine.await(robot.arm.moveTo(ArmPosition.STOWED));
	}).named("Auto.ScoreAndStow");
}
```
`coroutine.await(robot.intake.eject())` starts the eject command and waits for it to finish, `EJECT_TIME` later. Then the arm stows.
The routine uses two mechanisms, but it still doesn't need to require either one itself. Each command it awaits requires its own mechanism while it runs.

</details>
