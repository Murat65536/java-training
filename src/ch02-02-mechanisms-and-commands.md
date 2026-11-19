# Mechanisms and Commands

Time to swap the example mechanism for a real one: an arm. We'll build it the way we build every mechanism on our robot, with an IO interface like the one in [Interfaces](ch01-15-interfaces.md).

## Cleaning up
First, remove the example code we won't use:
1. Delete `mechanisms/ExampleMechanism.java` and `constants/ExampleConstants.java`. (Right-click a file on the left side of VS Code and pick **Delete**.)
2. In `Robot.java`, delete the `exampleMechanism` field and rename `exampleController` to `driverController`.
3. In `ExampleTeleop.java` and `ExampleAuto.java`, delete everything inside the constructors so they're empty for now. They'll be filled in again soon.

## The arm's IO
The point of IO is that the arm's code shouldn't care whether it's talking to a real motor or a pretend one. So the first step is an interface listing what the arm needs from its hardware. Make a new file in the `mechanisms` folder called `ArmIO.java`. (Right-click the folder, pick **New File**, and type the name.)
```java
package frc.robot.mechanisms;

public interface ArmIO {
	double getAngleDegrees();

	void setThrottle(double throttle);

	void update();
}
```
`getAngleDegrees` and `setThrottle` do the same jobs as `getAngle` and `setSpeed` from [Interfaces](ch01-15-interfaces.md), under new names. `getAngleDegrees` says what unit it uses, and `setThrottle` matches the name WPILib's motors use. `update` is new. The robot will call it on every `robotPeriodic()`, which gives the IO a chance to do whatever it needs to stay current.

For now we'll only make the pretend version, so it can run in the simulator. The arm's constants go in a new file, `constants/ArmConstants.java`:
```java
package frc.robot.constants;

public final class ArmConstants {
	public static final double MAX_THROTTLE = 0.5;
	public static final double TOLERANCE_DEGREES = 2.0;
	public static final double SIM_DEGREES_PER_UPDATE_AT_FULL_THROTTLE = 3.0;
}
```
And here's `mechanisms/SimArmIO.java`:
```java
package frc.robot.mechanisms;

import frc.robot.constants.ArmConstants;

public class SimArmIO implements ArmIO {
	private double angleDegrees;
	private double throttle;

	@Override
	public double getAngleDegrees() {
		return angleDegrees;
	}

	@Override
	public void setThrottle(double newThrottle) {
		throttle = newThrottle;
	}

	@Override
	public void update() {
		angleDegrees = angleDegrees + throttle * ArmConstants.SIM_DEGREES_PER_UPDATE_AT_FULL_THROTTLE;
	}
}
```
This pretend arm is about as simple as it gets. On every update it moves a little in whatever direction the throttle says. [Simulating an Arm](ch02-06-simulating-an-arm.md) replaces it with one that behaves like a real arm, gravity included.

The arm positions from [Enums](ch01-17-enums.md) come in handy too. Put this in `mechanisms/ArmPosition.java`:
```java
package frc.robot.mechanisms;

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

## The arm
Next, the arm itself, in `mechanisms/Arm.java`:
```java
package frc.robot.mechanisms;

import frc.robot.constants.ArmConstants;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.telemetry.Telemetry;

public class Arm implements Mechanism {
	private final ArmIO io;

	public Arm(ArmIO armIO) {
		io = armIO;
	}

	public double getAngleDegrees() {
		return io.getAngleDegrees();
	}

	public boolean isAt(ArmPosition position) {
		return Math.abs(getAngleDegrees() - position.getAngleDegrees()) < ArmConstants.TOLERANCE_DEGREES;
	}

	public void update() {
		io.update();
		Telemetry.log("Arm/Angle", getAngleDegrees());
	}

	private void setThrottle(double throttle) {
		io.setThrottle(Math.clamp(throttle, -ArmConstants.MAX_THROTTLE, ArmConstants.MAX_THROTTLE));
	}
}
```
Every piece of this has come up before:
- `io` is `private`, so nothing outside `Arm` can touch the hardware.
- `setThrottle` is `private` too, and it clamps the throttle as in [Decimals and Math](ch01-14-decimals-and-math.md). The arm can never go faster than `MAX_THROTTLE`.
- `isAt` is the "close enough" check from [Decimals and Math](ch01-14-decimals-and-math.md), with its tolerance in `ArmConstants`.
- `update` tells the IO to update, then logs the arm's angle with `Telemetry.log`. **Telemetry** is information the robot sends back for you to look at, a bit like a `println` that a dashboard can graph. `"Arm/Angle"` is the name it's sent under.

Nothing makes the arm move yet. That's what commands are for.

## Commands
A **command** is one thing for the robot to do: raise the arm, drive forward, shoot. Every action a robot takes happens through commands.
Here's a command that moves the arm to a position. Add it to `Arm`:
```java
public Command moveTo(ArmPosition position) {
	return run(coroutine -> {
		while (!isAt(position)) {
			if (getAngleDegrees() < position.getAngleDegrees()) {
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
It's nearly the same loop as in [Loops](ch01-18-loops.md). Until the arm reaches the position, it drives the arm toward it, up or down, and then calls `coroutine.yield()` to give the rest of the robot a turn. When the arm arrives, the loop ends, the arm stops, and the command is finished.
The position is part of the name, so the name comes out as something like `Arm.MoveTo[SCORING]`. Our command names always start with the mechanism's name and a `.`, which tells you which mechanism a command belongs to.
(Real arms use smarter math than "full speed until you get there," so they slow down smoothly near the target. The command still has the same shape: a loop that adjusts the motor and yields until the arm arrives.)

### Requirements
Commands made with `run` **require** the mechanism they came from, so `moveTo` requires the arm. Only one command can require a mechanism at a time. Otherwise two commands could fight over it, one raising the arm while the other lowers it.
If `moveTo(ArmPosition.SCORING)` is running and someone starts `moveTo(ArmPosition.STOWED)`, the scheduler **cancels** the first one and the new one takes over. A canceled command stops wherever it was and never gets another turn.

### When nothing is running
When no command is using the arm, like after `moveTo` finishes, the scheduler runs the arm's **default command**. Unless you choose a different one, every mechanism's default command is its `idle()` command, which comes from `Mechanism`.
`Mechanism`'s own `idle()` does nothing at all, and for an arm that's a problem. If a `moveTo` got canceled partway, the last throttle it set would stay in place, and the arm would keep moving. So the arm overrides `idle()` to make sure it's stopped:
```java
@Override
public Command idle() {
	return runRepeatedly(() -> setThrottle(0))
			.withPriority(Command.LOWEST_PRIORITY)
			.named("Arm.Idle");
}
```
- `runRepeatedly` is a shortcut for a command that runs the same thing over and over, forever. It writes the `while (true)` loop for you, `coroutine.yield()` included.
- `withPriority(Command.LOWEST_PRIORITY)` gives it the lowest possible **priority**. A command can only cancel another if its priority is the same or higher. The idle command has the lowest priority there is, so any other command can always take the arm from it.
- The calls are split across lines because one long line would be hard to read. It's still a single statement, so there's only one `;`, at the very end.

{{#quiz ../quizzes/ch02-02-commands.toml}}

## Hooking it up
Now the robot gets its arm. In `Robot.java`:
```java
public class Robot extends OpModeRobot {
	final Arm arm = new Arm(new SimArmIO());
	final CommandGamepad driverController =
			new CommandGamepad(DriverConstants.DRIVER_CONTROLLER_PORT);

	public Robot() {}

	@Override
	public void robotPeriodic() {
		arm.update();
		Scheduler.getDefault().run();
	}
}
```
`robotPeriodic()` updates the arm first, so the commands that run right after see its newest angle. (You'll need `import frc.robot.mechanisms.Arm;` and `import frc.robot.mechanisms.SimArmIO;` at the top. VS Code's Quick Fix can add them.)

Then, in `ExampleTeleop`, give the driver some buttons:
```java
public ExampleTeleop(Robot robot) {
	robot.driverController.faceDown().onTrue(robot.arm.moveTo(ArmPosition.STOWED));
	robot.driverController.faceRight().onTrue(robot.arm.moveTo(ArmPosition.INTAKE));
	robot.driverController.faceUp().onTrue(robot.arm.moveTo(ArmPosition.SCORING));
}
```
Pressing a button starts moving the arm to a position. You don't have to hold it. The command keeps going until the arm gets there or another button cancels it.

### Watching it
For now the arm is only a number, so the way to watch it is to watch the number. WPILib comes with a dashboard called **AdvantageScope** that can display telemetry:
1. Start the simulator, the same way as before.
2. Open the command palette, pick `WPILib: Start Tool`, and choose **AdvantageScope**.
3. In AdvantageScope, open the **File** menu and pick **Connect to Simulator**.
4. In the list on the left, find **Arm**, and inside it **Angle**. Drag **Angle** onto the graph.

Enable the robot in **Teleoperated** with `ExampleTeleop` selected and press the buttons. The arm's angle climbs to 80 when you press the top button and drops back to 0 when you press the bottom one. Try pressing a different button while the arm is moving. It turns around immediately, because the new command canceled the old one.

## The real arm
On the real robot, the arm uses a different IO that talks to the real motor. You don't need to add it to your project (it needs REV's motor library installed), but here's what it looks like:
```java
public class RealArmIO implements ArmIO {
	private final SparkMax motor = new SparkMax(CANPort.CAN_S0, ArmConstants.MOTOR_ID, MotorType.kBrushless);
	private final RelativeEncoder encoder = motor.getEncoder();

	@Override
	public double getAngleDegrees() {
		return encoder.getPosition() * ArmConstants.DEGREES_PER_MOTOR_ROTATION;
	}

	@Override
	public void setThrottle(double throttle) {
		motor.setThrottle(throttle);
	}

	@Override
	public void update() {}
}
```
A real motor and encoder keep themselves up to date, so `update` has nothing to do. `Robot` would make `new Arm(new RealArmIO())` instead, and *nothing else would change*. Every command and every button binding you write runs unchanged on the real robot.

{{#quiz ../quizzes/ch02-02-hooking-it-up.toml}}

## Try it yourself
Sometimes the driver needs to move the arm a little by hand, say to line up a shot. Add two commands to `Arm`:
- `nudgeUp()`, which moves the arm up at `MAX_THROTTLE` for as long as it runs.
- `nudgeDown()`, which does the same, but downward.

Then bind them in `ExampleTeleop` so the arm moves up while the right bumper is held and down while the left bumper is held. (Those are `rightBumper()` and `leftBumper()`.) When the driver lets go, the arm should stop.

*Hint: these commands have no position to wait for, so they never need to finish on their own. Which shortcut makes a command that repeats the same thing? And when the driver lets go, what runs on the arm?*

<details>
<summary>Answer</summary>

Here's one way to do it. In `Arm`:
```java
public Command nudgeUp() {
	return runRepeatedly(() -> setThrottle(ArmConstants.MAX_THROTTLE)).named("Arm.NudgeUp");
}

public Command nudgeDown() {
	return runRepeatedly(() -> setThrottle(-ArmConstants.MAX_THROTTLE)).named("Arm.NudgeDown");
}
```
And in `ExampleTeleop`'s constructor:
```java
robot.driverController.rightBumper().whileTrue(robot.arm.nudgeUp());
robot.driverController.leftBumper().whileTrue(robot.arm.nudgeDown());
```
`runRepeatedly` keeps setting the throttle for as long as the command runs. `whileTrue` starts the command when a bumper goes down and cancels it when the bumper comes up.
You didn't have to write anything to stop the arm. Once the nudge command is canceled, nothing is using the arm, so the scheduler runs its default command, `idle()`, which sets the throttle to 0. That's the reason `idle()` stops the arm.

</details>
