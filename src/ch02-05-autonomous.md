# Autonomous

Every FRC match opens with the **autonomous period**, a few seconds when the robot runs on its own and nobody touches the controllers. Whatever the robot does then comes from code written ahead of time.

## An autonomous opmode
An autonomous routine is an opmode like teleop, marked `@Autonomous` instead of `@Teleop`. Delete `ExampleAuto.java` and make a new file called `ScoreAndStowAuto.java` in the `frc/robot` folder:
```java
package frc.robot;

import frc.robot.mechanisms.ArmPosition;
import org.wpilib.command3.Trigger;
import org.wpilib.driverstation.RobotState;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.OpMode;

@Autonomous(name = "Score and Stow")
public class ScoreAndStowAuto implements OpMode {
	private final Trigger enabled = new Trigger(RobotState::isEnabled);

	public ScoreAndStowAuto(Robot robot) {
		enabled.onTrue(robot.arm.moveTo(ArmPosition.SCORING));
	}
}
```
The `enabled` trigger is there because WPILib makes an opmode as soon as the drivers pick it from the list. That's usually well before the match starts, while the robot is still disabled. A disabled robot can't run commands, so the constructor can't simply start the routine. It binds the routine to a trigger that turns true the moment the robot is enabled, when the match begins.
`RobotState::isEnabled` is a method reference to a `static` method, `RobotState.isEnabled()`. It works like `bob::isInDanger`, with a class name in front instead of an object.

Try it in the simulator. Pick **Autonomous** in Robot State, choose **Score and Stow** from the **OpMode** list, and the arm rises to the scoring position.

### When autonomous ends
When the autonomous period ends, the robot switches to teleop. Any autonomous commands still running don't carry over.
WPILib tracks which opmode made each trigger binding and each default command. When an opmode ends, WPILib cancels its commands and discards its bindings. Nothing from autonomous keeps running in teleop, and nothing from teleop runs in autonomous. This is also why it was safe for `DriverTeleop` to set the arm's default command to `manualControl`. That setting only lasts while `DriverTeleop` runs. At any other time, the arm's default command is `idle()` again.

## Doing one thing after another
Raising the arm is a start, but real autonomous routines do many things in sequence. Ours will raise the arm to score, wait a second, then lower it again.
The wait time is one of the routine's numbers, so it gets a new constants file in `constants`, `AutoConstants.java`:
```java
package frc.robot.constants;

import static org.wpilib.units.Units.Seconds;

import org.wpilib.units.measure.Time;

public final class AutoConstants {
	public static final Time SCORE_TIME = Seconds.of(1);
}
```

One way to build the routine is `andThen`, which makes a command that runs one command and then another:
```java
robot.arm.moveTo(ArmPosition.SCORING)
		.andThen(Command.waitFor(AutoConstants.SCORE_TIME).named("Wait"))
		.andThen(robot.arm.moveTo(ArmPosition.STOWED))
		.named("Auto.ScoreAndStow")
```
`Command.waitFor` makes a command that waits for a set amount of time. `andThen` returns a builder, as `run` does, so the whole chain still needs a `.named(...)` at the end.

That works, but a long chain of `andThen`s gets hard to read as a routine grows. There's another way that reads more like a list of steps. In `ScoreAndStowAuto`:
```java
public ScoreAndStowAuto(Robot robot) {
	enabled.onTrue(scoreAndStow(robot));
}

private Command scoreAndStow(Robot robot) {
	return Command.noRequirements(coroutine -> {
		coroutine.await(robot.arm.moveTo(ArmPosition.SCORING));
		coroutine.wait(AutoConstants.SCORE_TIME);
		coroutine.await(robot.arm.moveTo(ArmPosition.STOWED));
	}).named("Auto.ScoreAndStow");
}
```
(`ScoreAndStowAuto` needs `import org.wpilib.command3.Command;` and `import frc.robot.constants.AutoConstants;` for this.)
- `coroutine.await(command)` starts another command and waits for it to finish before moving to the next line. It keeps yielding while it waits, so the rest of the robot keeps running.
- `coroutine.wait(time)` waits for however long you tell it.
- `Command.noRequirements` makes a command that doesn't require any mechanism itself. It doesn't need to, because each command it awaits requires the arm while it runs.

The code reads the same as the routine itself: score, wait, stow. Adding a step means adding a line.

### Doing two things at once
A robot with only an arm can't do much beyond this. A real robot, though, might drive somewhere *while* raising the arm, to save time. `coroutine.awaitAll` starts several commands at once and waits until all of them have finished:
```java
coroutine.awaitAll(robot.drive.driveToGoal(), robot.arm.moveTo(ArmPosition.SCORING));
```
(We don't have a `drive` mechanism yet, so this is only to show what it looks like.) The two commands require different mechanisms, so they can run at the same time without conflict.

{{#quiz ../quizzes/ch02-05-autonomous.toml}}

## Doing nothing
Every robot needs one more autonomous opmode, one that does nothing at all. Drivers sometimes need it, for example when a mechanism is broken and moving it would make things worse. Make `DoNothingAuto.java`:
```java
package frc.robot;

import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.OpMode;

@Autonomous(name = "Do Nothing")
public class DoNothingAuto implements OpMode {
	public DoNothingAuto(Robot robot) {}
}
```
It doesn't have to do anything to keep the robot still. Every mechanism's default command is `idle()`, which already does that.

{{#quiz ../quizzes/ch02-05-opmodes.toml}}

## Try it yourself
Make a new autonomous opmode named **Wave** that waves the arm. It should move the arm up to the scoring position and back down to stowed, `WAVE_COUNT` times in a row. Make `WAVE_COUNT` 3, in `AutoConstants`.

*Hint: you want to repeat the same two steps several times. You know a way to do something a set number of times. Can it go inside a command?*

<details>
<summary>Answer</summary>

Here's one way to do it. In `AutoConstants`:
```java
public static final int WAVE_COUNT = 3;
```
And `WaveAuto.java`:
```java
package frc.robot;

import frc.robot.constants.AutoConstants;
import frc.robot.mechanisms.ArmPosition;
import org.wpilib.command3.Command;
import org.wpilib.command3.Trigger;
import org.wpilib.driverstation.RobotState;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.OpMode;

@Autonomous(name = "Wave")
public class WaveAuto implements OpMode {
	private final Trigger enabled = new Trigger(RobotState::isEnabled);

	public WaveAuto(Robot robot) {
		enabled.onTrue(wave(robot));
	}

	private Command wave(Robot robot) {
		return Command.noRequirements(coroutine -> {
			for (int i = 0; i < AutoConstants.WAVE_COUNT; i++) {
				coroutine.await(robot.arm.moveTo(ArmPosition.SCORING));
				coroutine.await(robot.arm.moveTo(ArmPosition.STOWED));
			}
		}).named("Auto.Wave");
	}
}
```
It's a `for` loop from [Loops](ch01-18-loops.md), inside a command. It doesn't need `coroutine.yield()`, because `coroutine.await` already yields while it waits for each move. Each trip around the loop takes as long as the two moves, and the rest of the robot keeps running throughout.

</details>
