# Real Hardware

Everything so far has run in the simulator, which is the right place to start because nothing can break. Eventually, though, your code has to drive a real robot, with real motors strong enough to hurt someone. This lesson covers getting from the simulator to the real thing safely.

## What's on the robot
The main electronic parts of an FRC robot, and what each one does:
- The **robot controller** is the robot's brain, the computer that runs our code. Starting in 2027, it's called **Systemcore**. (Older robots used one called the **roboRIO**, a name you'll see often in older code and documentation.)
- The **battery** powers everything through the **power distribution** board, which splits the power and sends it to every motor and device.
- Each motor has a **motor controller**, like REV's SPARK MAX, which decides how much of the battery's power reaches the motor. When our code calls `setVoltage`, the motor controller is what carries it out.
- The **radio** connects the robot to the Driver Station laptop over Wi-Fi.

### CAN
The robot controller talks to the motor controllers and many sensors over one long shared set of wires called the **CAN bus**. Every device is connected to the same wires, one after another, so each needs its own **CAN ID**, a number that says which device a message is meant for. That's what `ArmConstants.MOTOR_ID` is:
```java
private final SparkMax motor = new SparkMax(CANPort.CAN_S0, ArmConstants.MOTOR_ID, MotorType.kBrushless);
```
`CANPort.CAN_S0` says which of Systemcore's CAN ports the device is plugged into, and `MOTOR_ID` says which device it is. If two devices of the same kind share an ID, like two SPARK MAXes both set to 5, neither works properly, and the bug is confusing to track down. (Different kinds of devices, like a SPARK MAX and a CANcoder, can share an ID, but giving every device its own number keeps things simple.) You set a device's ID with its maker's app: the **REV Hardware Client** for REV devices, and **Phoenix Tuner X** for CTRE devices. Our team keeps a list of every device and its ID, and the IDs in the code have to match it.

## Vendor libraries
WPILib doesn't include code for every company's motors. Each company, or **vendor**, has its own library. To add one to a project, open the command palette, pick `WPILib: Manage Vendor Libraries`, then choose **Install new libraries (online)**. Our robot uses **REVLib** for REV's motors and **Phoenix 6** for CTRE's.
Each vendor also has its own documentation, which is where to look up how their classes work. Vendor libraries change every year, like WPILib, so check that what you're reading is for the right year.

## Setting up a motor
A motor needs a few settings before it can be trusted. Here's our arm's real IO with them:
```java
public class RealArmIO implements ArmIO {
	private final SparkMax motor = new SparkMax(CANPort.CAN_S0, ArmConstants.MOTOR_ID, MotorType.kBrushless);
	private final RelativeEncoder encoder = motor.getEncoder();

	public RealArmIO() {
		SparkMaxConfig config = new SparkMaxConfig();
		config.inverted(ArmConstants.MOTOR_INVERTED)
				.idleMode(IdleMode.kBrake)
				.smartCurrentLimit(ArmConstants.CURRENT_LIMIT_AMPS);
		motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
	}

	@Override
	public double getAngleDegrees() {
		return encoder.getPosition() * ArmConstants.DEGREES_PER_MOTOR_ROTATION;
	}

	@Override
	public void setVoltage(double volts) {
		motor.setVoltage(volts);
	}

	@Override
	public void update() {}
}
```
(These are REV's class names at the time of writing. REV renames things now and then, so if something here doesn't exist, check REV's documentation. The ideas stay the same.)
The constructor makes a `SparkMaxConfig`, fills in the settings and sends them to the motor controller with `configure`. What each setting does:
- **Inverted** flips the direction the motor spins. Depending on how a motor is mounted, positive voltage might turn it the "wrong" way, so the arm goes *down* when the code says up. Instead of scattering `-` signs through the code, you flip it once, here. Only testing tells you whether it needs flipping, so it's a constant.
- **Idle mode** sets what the motor does when it's told 0 volts. In **brake** mode it resists being turned, so the arm stays put instead of flopping. In **coast** mode it spins freely, which suits something like a flywheel that should spin down on its own.
- **Current limit** caps how much electricity the motor can draw. A stuck motor, like an arm pushing against a hard stop, can draw so much current that it overheats, trips a breaker, or drags the battery so low that the whole robot shuts down for a moment. That's called a **brownout**, and it's one of the most common ways robots die in matches. A current limit, in amps, prevents it.

`ResetMode.kResetSafeParameters` starts from REV's defaults before applying our settings, so nothing left over from before sneaks in. `PersistMode.kPersistParameters` saves the settings on the motor controller, so it keeps them after the robot loses power. (REV still names its options with a `k` in front, the older style.)

### Encoders and starting positions
The arm's encoder is built into the motor, and it's a **relative encoder**: it measures how far it has turned since the robot powered on. At power-on it reads 0, wherever the arm happens to be.
Our code treats 0 degrees as `STOWED`. So the arm *has to be stowed when the robot turns on*, or every angle will be off by however far up it was. That's one reason teams run a checklist before every match, and "mechanisms in their starting positions" is on it.
The other kind is an **absolute encoder**, which always knows the real angle, even right after power-on. Mechanisms that can't always start in the same place need one. CTRE's CANcoder, on each of our swerve modules, is an absolute encoder, so the robot always knows which way each wheel is pointing.

{{#quiz ../quizzes/ch02-11-motors.toml}}

## Deploying
To put your code on the robot:
1. Connect your laptop to the robot. The robot's Wi-Fi is the usual way, but a cable works too.
2. Open the command palette and pick `WPILib: Deploy Robot Code`.
3. Watch the terminal. It builds the code, sends it to the robot and starts it. If the build fails, nothing is sent, and the robot keeps running whatever it had before.

Then open the **Driver Station**, the program the drive team uses to run the robot. Check that it shows communication with the robot, that robot code is running, and that the controllers are plugged in. Then pick your opmodes. Have a mentor show you around it the first time.

## Running new code for the first time
New code on a real robot is the riskiest moment in robot programming. The simulator catches most mistakes, but not all of them: an inverted motor, a wrong CAN ID, a constant that's far too big. So we follow the same steps every time:
1. **Tell everyone.** Say "enabling!" loudly every time before you enable, and make sure nobody's hands are near the robot.
2. **Know how to stop it.** Before enabling anything, have a mentor show you the Driver Station's keys for disabling and for emergency stop, and keep your hand ready. An emergency stop shuts everything off at once, and the robot stays off until it's restarted.
3. **Put it on blocks.** When testing driving, prop the robot up so the wheels are off the ground. A drivetrain that runs backward can't go anywhere on blocks.
4. **Start slow.** Turn `MAX_VOLTAGE` way down for the first test, to 1 or 2 volts, so that if something goes wrong, it goes wrong slowly.
5. **Check directions first.** Nudge the mechanism up with a button. Does it really go up? Does the angle on the dashboard go *up* too? If the motor goes the wrong way, flip `MOTOR_INVERTED`. If the motor is right but the angle goes the wrong way, the encoder is backward. A NEO's built-in encoder flips along with the motor, so this only happens with a separate encoder, and that encoder has its own inverted setting.
6. **Then tune.** Start from the gains you found in the simulator and tune them on the real thing, beginning with `KG`, the same way you did in [Simulating an Arm](ch02-06-simulating-an-arm.md).
7. **Turn the speed back up** a little at a time, once everything works.

## Try it yourself
A **soft limit** is a limit in the code that stops a mechanism before it reaches its hard stop, the piece of metal it physically can't move past. Hitting a hard stop at full speed is rough on a mechanism, and pushing against one is exactly what draws too much current.
Add soft limits to `Arm`. If the arm is at or above `SOFT_MAX_ANGLE`, `setVoltage` shouldn't let it push any higher, and if it's at or below `SOFT_MIN_ANGLE`, it shouldn't push any lower. It should still be able to move *away* from a limit. Make `SOFT_MAX_ANGLE` 95 degrees and `SOFT_MIN_ANGLE` 0 degrees, in `ArmConstants`.

*Hint: which direction is "up," in volts? And an `Angle` has comparison methods like `gte` and `lte`.*

To test it, use `nudgeUp` to push the arm as high as it will go. It should stop at about 95 degrees instead of 100.

<details>
<summary>Answer</summary>

Here's one way to do it. In `ArmConstants`:
```java
public static final Angle SOFT_MAX_ANGLE = Degrees.of(95);
public static final Angle SOFT_MIN_ANGLE = Degrees.of(0);
```
And in `Arm`:
```java
private void setVoltage(double volts) {
	double limitedVolts = Math.clamp(volts, -ArmConstants.MAX_VOLTAGE, ArmConstants.MAX_VOLTAGE);
	if (getAngle().gte(ArmConstants.SOFT_MAX_ANGLE) && limitedVolts > 0) {
		limitedVolts = 0;
	}
	if (getAngle().lte(ArmConstants.SOFT_MIN_ANGLE) && limitedVolts < 0) {
		limitedVolts = 0;
	}
	io.setVoltage(limitedVolts);
}
```
Positive volts push the arm up, so at the top only positive volts are blocked. Negative volts can still bring it down, so the arm never gets stuck at a limit.
Every command goes through `setVoltage`, so this one change protects the arm from all of them: `moveTo`, `manualControl`, the nudges, and any command written later. That's one of the best reasons to keep `setVoltage` `private` and route every command through it.
(Motor controllers can also enforce soft limits themselves, as one of their settings. Either way works, and some teams use both to be safe.)

</details>
