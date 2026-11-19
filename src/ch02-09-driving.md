# Driving

Every robot has a **drivetrain**, the wheels and the motors that drive them. Ours is a **swerve drive**. Each corner of the robot has a **swerve module** with two motors. One spins the wheel, and the other turns the whole wheel to point any direction. A swerve robot can drive forward, sideways or diagonally, and spin at the same time.
This lesson gives our robot a drivetrain and drives it around a pretend field. Before that, we need a way to describe *where* the robot is.

## Where is the robot?
WPILib treats the field like a big sheet of graph paper measured in meters. Picture yourself standing behind the blue alliance's wall, looking out at the field:
- The corner on your right is **(0, 0)**, called the **origin**.
- **x** runs straight out, away from you, toward the red alliance's wall.
- **y** runs to your left, along the blue wall.
- Angles are measured from the x direction and go **counterclockwise**. 0 degrees faces the red wall, 90 degrees faces left, and 180 degrees faces back toward you.

Every robot on the field uses this same map, whatever its alliance. WPILib has a class for each piece of it:
```java
Translation2d spot = new Translation2d(Meters.of(3), Meters.of(2));
Rotation2d facing = new Rotation2d(Degrees.of(90));
Pose2d robotPose = new Pose2d(Meters.of(3), Meters.of(2), facing);
```
- A `Translation2d` is a spot on the field, here 3 meters out from the blue wall and 2 meters left of the origin.
- A `Rotation2d` is the direction something faces.
- A `Pose2d` is both: a spot, plus the direction the robot faces there. "Where is the robot?" really asks "what's the robot's **pose**?"

You can pull the pieces back out with methods like `robotPose.getX()` and `robotPose.getY()` (in meters), `robotPose.getTranslation()` and `robotPose.getRotation().getDegrees()`. `getDistance` tells you how far apart two translations are, and constants like `Rotation2d.ZERO` face straight down the field. (All of these are in the `org.wpilib.math.geometry` package.)

## How fast?
A pose says where the robot is. `ChassisVelocities` says how it should *move*. ("Chassis" means the robot's frame.) It has three parts:
```java
ChassisVelocities velocities = new ChassisVelocities(
		MetersPerSecond.of(1),
		MetersPerSecond.of(0.5),
		DegreesPerSecond.of(45));
```
1. Speed in the x direction.
2. Speed in the y direction.
3. Turning speed, counterclockwise.

That leaves the question of which way x and y point. There are two answers:
- **Robot-relative**: x is wherever the front of the robot points, and y is the robot's left. "Go forward" means "go the way the robot is facing."
- **Field-relative**: x and y are the field's directions. "Go forward" means "go toward the red wall," whichever way the robot faces.

Drivers nearly always want field-relative. They stand at one end of the field, and when they push the stick away from themselves, they want the robot to move away from them, even if it's spun around backward. A swerve drive can move in any direction regardless of where it's facing, so this works perfectly.
The motors need robot-relative directions, though, so there's a method to convert. Given the direction the robot faces, `fieldRelative.toRobotRelative(facing)` returns the same movement in the robot's directions.

{{#quiz ../quizzes/ch02-09-poses.toml}}

## The drivetrain's IO
Real swerve code is complicated. It has to work out what each of the four modules should do and keep track of where the robot is, using every wheel's encoder and a gyro. Fortunately, we don't write most of it. Our drivetrain uses CTRE motors and sensors, and CTRE's app, **Phoenix Tuner X**, generates the swerve code once we describe our robot to it. The generated code takes a `ChassisVelocities`, works out everything else, and keeps track of the robot's pose.
So our drivetrain's IO can talk about the whole robot instead of individual motors. Make `mechanisms/DriveIO.java`:
```java
package frc.robot.mechanisms;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.kinematics.ChassisVelocities;

public interface DriveIO {
	Pose2d getPose();

	void setVelocities(ChassisVelocities robotRelativeVelocities);

	void update();
}
```
On the real robot, `RealDriveIO` passes the velocities to CTRE's generated code and asks it for the pose. In the simulator, the robot is perfect: it moves at exactly the speed it's told, instantly. First, `constants/DriveConstants.java`:
```java
package frc.robot.constants;

import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.MetersPerSecond;
import static org.wpilib.units.Units.RotationsPerSecond;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.LinearVelocity;

public final class DriveConstants {
	public static final LinearVelocity MAX_SPEED = MetersPerSecond.of(4);
	public static final AngularVelocity MAX_TURN_SPEED = RotationsPerSecond.of(1);
	public static final Pose2d SIM_STARTING_POSE = new Pose2d(Meters.of(2), Meters.of(4), Rotation2d.ZERO);
}
```
`LinearVelocity` and `AngularVelocity` are units types like `Angle` and `Time`. The robot can drive 4 meters a second and spin one full turn a second.
Then `mechanisms/SimDriveIO.java`:
```java
package frc.robot.mechanisms;

import frc.robot.constants.DriveConstants;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.kinematics.ChassisVelocities;

public class SimDriveIO implements DriveIO {
	private Pose2d pose = DriveConstants.SIM_STARTING_POSE;
	private ChassisVelocities velocities = new ChassisVelocities();

	@Override
	public Pose2d getPose() {
		return pose;
	}

	@Override
	public void setVelocities(ChassisVelocities robotRelativeVelocities) {
		velocities = robotRelativeVelocities;
	}

	@Override
	public void update() {
		pose = pose.plus(velocities.toTwist2d(OpModeRobot.DEFAULT_PERIOD).exp());
	}
}
```
`new ChassisVelocities()`, with nothing in the parentheses, is all zeros, meaning sitting still. The line in `update` moves the pretend robot by one update's worth of driving:
- `velocities.toTwist2d(OpModeRobot.DEFAULT_PERIOD)` works out how far the robot would move in one update, 0.02 seconds, at these velocities.
- `exp()` turns that into a move the pose understands, and `pose.plus(...)` makes the new pose after that move. The move is robot-relative, which matches our velocities.

## The drivetrain
Now `mechanisms/Drive.java`:
```java
package frc.robot.mechanisms;

import java.util.function.Supplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.kinematics.ChassisVelocities;
import org.wpilib.telemetry.Telemetry;

public class Drive implements Mechanism {
	private final DriveIO io;

	public Drive(DriveIO driveIO) {
		io = driveIO;
	}

	public Pose2d getPose() {
		return io.getPose();
	}

	public void update() {
		io.update();
		Telemetry.log("Drive/Pose", getPose());
	}

	private void driveFieldRelative(ChassisVelocities fieldRelativeVelocities) {
		io.setVelocities(fieldRelativeVelocities.toRobotRelative(getPose().getRotation()));
	}

	public Command driverControl(Supplier<ChassisVelocities> velocities) {
		return runRepeatedly(() -> driveFieldRelative(velocities.get()))
				.withPriority(Command.LOWEST_PRIORITY)
				.named("Drive.DriverControl");
	}

	@Override
	public Command idle() {
		return runRepeatedly(() -> io.setVelocities(new ChassisVelocities()))
				.withPriority(Command.LOWEST_PRIORITY)
				.named("Drive.Idle");
	}
}
```
- `driveFieldRelative` takes field-relative velocities, converts them to robot-relative using the direction the robot faces right now, and passes them to the IO.
- `driverControl` works much like the arm's `manualControl`. It takes a `Supplier<ChassisVelocities>`, which is like a `DoubleSupplier` that supplies a `ChassisVelocities` instead of a `double`. On every trip around, it asks for the latest velocities and drives with them.
- `idle()` stops the robot, so it sits still whenever nothing is driving it.
- `update` logs the robot's pose, so we can watch it on a map of the field.

In `Robot.java`, add the drivetrain and update it in `robotPeriodic()`:
```java
final Drive drive = new Drive(new SimDriveIO());
```
```java
drive.update();
```

## Two controllers
Driving needs sticks, but the driver's left stick already moves the arm. Real teams handle this by having two people drive the robot. The **driver** handles the drivetrain, and the **operator** runs everything else from a second controller.
In `DriverConstants`, add a port for the operator's controller:
```java
public static final int OPERATOR_CONTROLLER_PORT = 2;
```
In `Robot`, add a second controller:
```java
final CommandGamepad operatorController =
		new CommandGamepad(DriverConstants.OPERATOR_CONTROLLER_PORT);
```
Then, in `DriverTeleop`, change every `robot.driverController` to `robot.operatorController`, which makes the arm and intake the operator's job and frees the driver's controller for driving.

The driver's sticks should work like this:
- The left stick drives. Push it away from you to go down the field, and left to go left.
- The right stick turns. Push it left to spin left (counterclockwise), and right to spin right.

Pushing a stick forward or left gives a *negative* number, so all three need a `-`. Add this at the start of `DriverTeleop`'s constructor:
```java
robot.drive.setDefaultCommand(robot.drive.driverControl(() -> new ChassisVelocities(
		DriveConstants.MAX_SPEED.times(-robot.driverController.getLeftY()),
		DriveConstants.MAX_SPEED.times(-robot.driverController.getLeftX()),
		DriveConstants.MAX_TURN_SPEED.times(-robot.driverController.getRightX()))));
```
`MAX_SPEED.times(...)` multiplies a measurement by a number and keeps the unit, so the stick chooses how much of the top speed to use. The deadband still makes each released stick exactly 0, so the robot stops. (`DriverTeleop` needs imports for `ChassisVelocities` and `DriveConstants`.)

### Watching it
Start the simulator. Put a controller in slot 1 of **Joysticks** for the driver and one in slot 2 for the operator. (With only one controller, use the keyboard for the other.)
In AdvantageScope, click the **+** and choose **2D Field**. Find **Drive**, then **Pose**, and drag it onto the field. A small robot appears. Enable the robot in **Teleoperated** and drive it around. Spin it with the right stick, then push the left stick forward. It still heads down the field, whichever way it's facing.

### Red alliance
One problem remains. On the red alliance, the drivers stand at the *other* end of the field. Pushing the stick away from them should drive toward the blue wall, which is the *negative* x direction, and their left is the field's negative y. So on red, both of the left stick's directions need to flip.
WPILib reports our alliance through `MatchState.getAlliance()`. Add this method to `DriverTeleop`:
```java
private static boolean isRedAlliance() {
	return MatchState.getAlliance().orElse(Alliance.BLUE) == Alliance.RED;
}
```
`getAlliance()` returns an `Optional<Alliance>`. An **`Optional`** is a box that might hold something or might be empty. This one is empty when the robot doesn't know its alliance yet, like before it connects to the field. `orElse(Alliance.BLUE)` opens the box. It returns what's inside, or `Alliance.BLUE` if the box is empty. Then the method checks whether that's `RED`. (`MatchState` and `Alliance` are in `org.wpilib.driverstation`.)

The driving lambda can now flip the left stick on the red alliance, using a ternary from [More Decisions](ch01-08-more-decisions.md):
```java
robot.drive.setDefaultCommand(robot.drive.driverControl(() -> {
	double flip = isRedAlliance() ? -1 : 1;
	return new ChassisVelocities(
			DriveConstants.MAX_SPEED.times(-robot.driverController.getLeftY() * flip),
			DriveConstants.MAX_SPEED.times(-robot.driverController.getLeftX() * flip),
			DriveConstants.MAX_TURN_SPEED.times(-robot.driverController.getRightX()));
}));
```
Turning doesn't flip, because counterclockwise is counterclockwise from either end of the field.
To test it, open the **FMS** window in the Sim GUI and set **Alliance Station** to one of the red stations.

{{#quiz ../quizzes/ch02-09-driving.toml}}

## Driving to a spot
In autonomous there's no driver, so the robot has to drive itself where it needs to go. PID, from [Feedback Control](ch02-07-feedback-control.md), already gets something to a target smoothly. A drivetrain has three things to control instead of one, x, y and heading, so it uses three `PIDController`s.
Add these to `DriveConstants`:
```java
public static final double TRANSLATION_KP = 2.0;
public static final double HEADING_KP = 4.0;
public static final Distance POSITION_TOLERANCE = Meters.of(0.05);
public static final Angle HEADING_TOLERANCE = Degrees.of(2);
```
These two `KP`s return speeds instead of volts. `TRANSLATION_KP` is meters per second for every meter of error, and `HEADING_KP` is radians per second for every radian. (`DriveConstants` now needs imports for `Distance`, `Angle` and `Degrees`.)
Then, in `Drive`:
```java
private final PIDController xController = new PIDController(DriveConstants.TRANSLATION_KP, 0, 0);
private final PIDController yController = new PIDController(DriveConstants.TRANSLATION_KP, 0, 0);
private final PIDController headingController = new PIDController(DriveConstants.HEADING_KP, 0, 0);

public Drive(DriveIO driveIO) {
	io = driveIO;
	headingController.enableContinuousInput(-Math.PI, Math.PI);
}
```
`enableContinuousInput` fixes a problem only angles have. Facing 179 degrees and facing -179 degrees are only 2 degrees apart, but the plain difference between the numbers is 358. Without this line, the robot would spin nearly all the way around the long way. `enableContinuousInput(-Math.PI, Math.PI)` tells the controller that angles wrap around at half a turn each way (in radians), so it always turns the short way.

Next, a check for whether the robot has arrived, and the command itself:
```java
public boolean isAt(Pose2d target) {
	Pose2d pose = getPose();
	double distanceMeters = pose.getTranslation().getDistance(target.getTranslation());
	double headingErrorDegrees = Math.abs(pose.getRotation().minus(target.getRotation()).getDegrees());
	return distanceMeters < DriveConstants.POSITION_TOLERANCE.in(Meters)
			&& headingErrorDegrees < DriveConstants.HEADING_TOLERANCE.in(Degrees);
}

public Command driveTo(Pose2d target) {
	return run(coroutine -> {
		double maxSpeed = DriveConstants.MAX_SPEED.in(MetersPerSecond);
		double maxTurnSpeed = DriveConstants.MAX_TURN_SPEED.in(RadiansPerSecond);
		while (!isAt(target)) {
			Pose2d pose = getPose();
			double xSpeed = xController.calculate(pose.getX(), target.getX());
			double ySpeed = yController.calculate(pose.getY(), target.getY());
			double turnSpeed = headingController.calculate(
					pose.getRotation().getRadians(), target.getRotation().getRadians());
			driveFieldRelative(new ChassisVelocities(
					Math.clamp(xSpeed, -maxSpeed, maxSpeed),
					Math.clamp(ySpeed, -maxSpeed, maxSpeed),
					Math.clamp(turnSpeed, -maxTurnSpeed, maxTurnSpeed)));
			coroutine.yield();
		}
	}).named("Drive.DriveTo");
}
```
It follows the same pattern as the arm's `moveTo`: until the robot arrives, use PID to work out how hard to push, push, and yield. Each controller handles one part, so `xController` only looks at x, and so on. The speeds are clamped so the robot never tries to go faster than it can. `ChassisVelocities` also has a constructor that takes plain `double`s in meters per second and radians per second, which is what our controllers return.
(`Drive` needs an import for `PIDController`, plus `import static`s for `Meters`, `Degrees`, `MetersPerSecond` and `RadiansPerSecond`.)

Real robots usually follow a **path** instead, a smooth curve planned ahead of time in an app like PathPlanner or Choreo. A path is a lot like a motion profile for a drivetrain, and [What's Next](ch02-14-whats-next.md) covers it. Underneath, path following uses PID controllers like these.

### Where is the robot, really?
Our pretend robot always knows exactly where it is. A real robot has to work it out. CTRE's code tracks how far every wheel has turned, uses the gyro to know which way the robot is facing, and adds it all up. This is called **odometry**. Wheels slip and robots get bumped, though, so odometry slowly drifts from the truth over a match. That's why most robots also use cameras, which spot markers on the field and correct the pose. [What's Next](ch02-14-whats-next.md) has more on this as well.
It's also why autonomous routines usually begin by telling the robot exactly where it is. The drivers line it up in the same starting spot every match, and the code sets the pose to match.

## Try it yourself
Make a new autonomous opmode called **Drive and Score**. It should:
1. Drive to `AutoConstants.SCORING_POSE` while raising the arm to the scoring position *at the same time*.
2. Eject the game piece.
3. Stow the arm.

Put `SCORING_POSE` 4 meters out from the blue wall and 6 meters left of the origin, facing 90 degrees.

*Hint: [Autonomous](ch02-05-autonomous.md) showed a way to run two commands at once and wait for both. You can also make a `Rotation2d` from an `Angle`.*

To test it, restart the simulator so the robot starts at `SIM_STARTING_POSE`, and watch it on the **2D Field** tab.

<details>
<summary>Answer</summary>

Here's one way to do it. In `AutoConstants`:
```java
public static final Pose2d SCORING_POSE = new Pose2d(Meters.of(4), Meters.of(6), new Rotation2d(Degrees.of(90)));
```
And `DriveAndScoreAuto.java`:
```java
package frc.robot;

import frc.robot.constants.AutoConstants;
import frc.robot.mechanisms.ArmPosition;
import org.wpilib.command3.Command;
import org.wpilib.command3.Trigger;
import org.wpilib.driverstation.RobotState;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.OpMode;

@Autonomous(name = "Drive and Score")
public class DriveAndScoreAuto implements OpMode {
	private final Trigger enabled = new Trigger(RobotState::isEnabled);

	public DriveAndScoreAuto(Robot robot) {
		enabled.onTrue(driveAndScore(robot));
	}

	private Command driveAndScore(Robot robot) {
		return Command.noRequirements(coroutine -> {
			coroutine.awaitAll(
					robot.drive.driveTo(AutoConstants.SCORING_POSE),
					robot.arm.moveTo(ArmPosition.SCORING));
			coroutine.await(robot.intake.eject());
			coroutine.await(robot.arm.moveTo(ArmPosition.STOWED));
		}).named("Auto.DriveAndScore");
	}
}
```
`awaitAll` starts driving and raising the arm at the same moment and waits until both are done. They require different mechanisms, so they don't conflict. It's the same kind of `awaitAll` [Autonomous](ch02-05-autonomous.md) showed, which we couldn't run then because there was no drivetrain yet.
The robot glides toward the spot, turning to face 90 degrees on the way, and slows as it gets close thanks to PID.

</details>
