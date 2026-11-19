# Controllers and Teleop

## A real teleop opmode
`ExampleTeleop` has served its purpose, and it's time for an opmode of our own. Make a new file in the `frc/robot` folder called `DriverTeleop.java`:
```java
package frc.robot;

import frc.robot.mechanisms.ArmPosition;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Teleop;

@Teleop(name = "Driver")
public class DriverTeleop implements OpMode {
	public DriverTeleop(Robot robot) {
		robot.driverController.faceDown().onTrue(robot.arm.moveTo(ArmPosition.STOWED));
		robot.driverController.faceRight().onTrue(robot.arm.moveTo(ArmPosition.INTAKE));
		robot.driverController.faceUp().onTrue(robot.arm.moveTo(ArmPosition.SCORING));
		robot.driverController.rightBumper().whileTrue(robot.arm.nudgeUp());
		robot.driverController.leftBumper().whileTrue(robot.arm.nudgeDown());
	}
}
```
Then delete `ExampleTeleop.java`.
`@Teleop(name = "Driver")` gives the opmode a name for the Driver Station's list, in place of its class name. That helps once a robot has several teleop opmodes, like one for matches and one for letting visitors drive at a demo.

## Buttons
A `CommandGamepad` has a method for each button, and each returns a `Trigger` that's true while that button is held:

| Method                                             | Button                                                     |
| -------------------------------------------------- | ---------------------------------------------------------- |
| `faceDown()`, `faceRight()`, `faceLeft()`, `faceUp()` | The four buttons on the right side, named by where they are |
| `leftBumper()`, `rightBumper()`                     | The buttons on top, above the triggers                     |
| `dpadUp()`, `dpadDown()`, `dpadLeft()`, `dpadRight()` | The four arrows on the left side                          |
| `leftStick()`, `rightStick()`                       | Pressing down on a stick                                   |
| `start()`, `back()`                                 | The small buttons in the middle                            |

Different controllers print different letters or shapes on their face buttons, so WPILib names them by position. `faceDown()` is the bottom one on every controller.

### Bindings
A trigger can run a command in several ways:
- **`onTrue(command)`** starts the command when the trigger becomes true, like when a button is pressed. The command then runs until it finishes or something cancels it, even after the button is released.
- **`whileTrue(command)`** starts the command when the trigger becomes true and cancels it as soon as the trigger becomes false.
- **`onFalse(command)`** starts the command when the trigger becomes *false*, like when a button is released.
- **`toggleOnTrue(command)`** starts the command on the first press and cancels it on the next, like a light switch.

Use `onTrue` for actions that have an end, like moving to a position. Use `whileTrue` for actions the driver controls by holding a button, like nudging.

Triggers can also be combined with `and`, `or` and `negate`:
```java
Trigger bothBumpers = robot.driverController.leftBumper().and(robot.driverController.rightBumper());
```
`bothBumpers` is true only while *both* bumpers are held. That's good for anything that shouldn't happen by accident, like a climb that can't be undone, since pressing two buttons at once by mistake is unlikely.
`and` and `or` take a `BooleanSupplier`, so any condition works, not only buttons. `robot.driverController.faceUp().and(() -> !robot.arm.isAt(ArmPosition.SCORING))`, for example, is true only when the top button is pressed *and* the arm isn't already at the scoring position.

{{#quiz ../quizzes/ch02-04-buttons.toml}}

## Joysticks
A button is either pressed or not. A stick tells you how far it's pushed, from -1 to 1.

| Method                      | Gives back                                                   |
| --------------------------- | ------------------------------------------------------------ |
| `getLeftX()`, `getRightX()` | How far the stick is pushed to the side. Right is positive.  |
| `getLeftY()`, `getRightY()` | How far the stick is pushed up or down. **Back** is positive. |
| `getLeftTrigger()`, `getRightTrigger()` | How far a trigger is squeezed, from 0 to 1.      |

Pushing a stick *forward* gives a negative number, which surprises everyone at first. That's why you'll nearly always see a `-` in front of `getLeftY()` and `getRightY()`, as in [Lambdas](ch01-16-lambdas.md).
A released stick never sits at exactly 0. It might rest at 0.02 or -0.04, and a robot that obeyed those numbers would creep across the field on its own. So WPILib's gamepads have a small **deadband**: anything closer to 0 than 0.1 counts as exactly 0.

### Driving the arm with a stick
Next, the driver gets to move the arm with the left stick. The command goes in `Arm`:
```java
public Command manualControl(DoubleSupplier throttle) {
	return runRepeatedly(() -> setThrottle(throttle.getAsDouble() * ArmConstants.MAX_THROTTLE))
			.withPriority(Command.LOWEST_PRIORITY)
			.named("Arm.ManualControl");
}
```
`manualControl` takes a `DoubleSupplier`, not a `double`, for the reason given in [Lambdas](ch01-16-lambdas.md). A `double` would only hold where the stick was at the moment the command was made. A `DoubleSupplier` can be asked again on every trip around, so the command always knows where the stick is *right now*.
Each time around, it multiplies the stick by `MAX_THROTTLE`. Pushing the stick all the way gives the arm's top speed, and pushing it halfway gives half that.
(`manualControl` doesn't need to know anything about controllers. It only needs *some* way to get a number. You could hand it a lambda that reads a stick, a button or anything else.)

When should it run? Whenever the driver isn't pressing a position button, which is what a default command is for. Add this to `DriverTeleop`'s constructor:
```java
robot.arm.setDefaultCommand(robot.arm.manualControl(() -> -robot.driverController.getLeftY()));
```
Whenever no other command is using the arm, it now follows the left stick. When the driver presses a position button, `moveTo` takes over, and when it finishes, the arm goes back to following the stick. Let go of the stick and the deadband makes it exactly 0, so the arm stops.
`manualControl` has `.withPriority(Command.LOWEST_PRIORITY)`. WPILib recommends the lowest priority for every default command, like `idle()`, so any other command can always take over.

{{#quiz ../quizzes/ch02-04-joysticks.toml}}

## Try it yourself
When the driver is lining up a shot, the arm moves too fast to place precisely. Add a slow mode. While the driver squeezes the right trigger past `SLOW_MODE_TRIGGER_THRESHOLD`, `manualControl` should move the arm at `SLOW_MODE_MULTIPLIER` of its normal speed. Put both in `ArmConstants`, with the threshold at `0.5` (squeezed halfway) and the multiplier at `0.25` (a quarter of the speed).

*Hint: `manualControl` doesn't need to change at all. It already does whatever its `DoubleSupplier` says. What could the lambda you give it do differently?*

<details>
<summary>Answer</summary>

Here's one way to do it. In `ArmConstants`:
```java
public static final double SLOW_MODE_TRIGGER_THRESHOLD = 0.5;
public static final double SLOW_MODE_MULTIPLIER = 0.25;
```
And in `DriverTeleop`, give `manualControl` a lambda that checks the trigger:
```java
robot.arm.setDefaultCommand(robot.arm.manualControl(() -> {
	double stick = -robot.driverController.getLeftY();
	if (robot.driverController.getRightTrigger() > ArmConstants.SLOW_MODE_TRIGGER_THRESHOLD) {
		return stick * ArmConstants.SLOW_MODE_MULTIPLIER;
	}
	return stick;
}));
```
The lambda is more than one line now, so it uses `{ }` and `return`, as in [Lambdas](ch01-16-lambdas.md). `manualControl` doesn't know or care what the lambda does. It asks for a number on every trip around, and that number is smaller while the trigger is held.
The code checks whether the trigger is *past* a threshold rather than checking for exactly `1.0`. As [Close enough](ch01-14-decimals-and-math.md#close-enough) said, don't use `==` with `double`s. A trigger squeezed "all the way" might only read `0.98`.

</details>
