# Feedback Control

Right now `moveTo` drives the arm at full speed until it's close enough, then cuts the motor. Start the simulator, connect AdvantageScope, and watch the **Arm/Angle** graph while you move the arm from stowed to scoring. The line climbs in a straight, steep slope and then flattens out all at once, leaving a sharp corner at the top. The arm moves at full speed, about 85 degrees a second, right up to the moment it's close enough, and then the motor stops dead.
On a real robot, a stop that sudden shakes the whole robot and can fling a game piece out of the intake. A lighter, faster arm wouldn't even stop in the right place. It would sail past the target, turn around, overshoot the other way, and keep swinging back and forth.

The arm should slow down as it gets close, the way you ease off before a stop sign instead of slamming the brakes at the last second. That's **feedback control**: the code keeps measuring how far off the arm is and uses that to decide how hard to push.

## Volts, not throttle
First, a small change that real robot code always makes. So far we've given the motor a throttle, a fraction of the battery's voltage. But a battery's voltage doesn't stay put. A fresh one might be at 12.5 volts, and by the end of a match it might be down to 11. A throttle of 0.5 gives about 6.2 volts at the start of the match and only 5.5 at the end, so the arm behaves differently depending on how tired the battery is.
Give the motor a **voltage** instead, and the motor controller works out the throttle from whatever the battery has at the moment. 6 volts is always 6 volts.

Change `ArmIO` to take a voltage:
```java
public interface ArmIO {
	double getAngleDegrees();

	void setVoltage(double volts);

	void update();
}
```
The simulator already wanted volts, so `SimArmIO` gets simpler:
```java
private double volts;

@Override
public void setVoltage(double newVolts) {
	volts = newVolts;
}

@Override
public void update() {
	armSim.setInputVoltage(volts);
	armSim.update(OpModeRobot.DEFAULT_PERIOD);
}
```
(Delete the `throttle` field. `SimArmIO` doesn't use `RobotController` anymore either, so that import can go.) On the real robot, `RealArmIO` would call `motor.setVoltage(volts)` instead of `motor.setThrottle(throttle)`.

In `ArmConstants`, replace `MAX_THROTTLE` with a voltage. The old top throttle was 0.5, and half of a 12-volt battery is 6 volts:
```java
public static final double MAX_VOLTAGE = 6.0;
```
Then, in `Arm`, rename `setThrottle` to `setVoltage` and use `MAX_VOLTAGE` wherever `MAX_THROTTLE` was:
```java
private void setVoltage(double volts) {
	io.setVoltage(Math.clamp(volts, -ArmConstants.MAX_VOLTAGE, ArmConstants.MAX_VOLTAGE));
}
```
So `nudgeUp` sets `ArmConstants.MAX_VOLTAGE`, and `manualControl` multiplies the stick by `ArmConstants.MAX_VOLTAGE`. The stick still gives a number from -1 to 1, so pushing it all the way still gives the arm's top speed.

## Feedforward
`gravityThrottle` needs to be in volts too. Multiplying by 12 would work, but this is a good moment to meet the WPILib class real code uses for it, `ArmFeedforward`. As [Simulating an Arm](ch02-06-simulating-an-arm.md) explained, **feedforward** means working out in advance what the motor needs, like how hard to push to hold the arm up. `ArmFeedforward` does that math.
It takes three numbers that show up in nearly every mechanism's constants:
- **`KS`** ("static"): the voltage needed to get past friction and start moving at all.
- **`KG`** ("gravity"): the voltage needed to hold the arm up when it's level. This is our `GRAVITY_THROTTLE`, in volts.
- **`KV`** ("velocity"): how many more volts each bit of speed takes.

Our pretend arm has no friction, and feedforward is only holding the arm up here, so `KS` and `KV` stay 0. `KG` is your `GRAVITY_THROTTLE` times 12. In `ArmConstants`, replace `GRAVITY_THROTTLE` with:
```java
public static final double KS = 0;
public static final double KG = 0.27;
public static final double KV = 0;
```
(Older code and much of the documentation write these as `kS`, `kG` and `kV`, with a lowercase `k` in front. That's an older naming style for constants.)

Next, give `Arm` a feedforward and turn `gravityThrottle` into `gravityVolts`:
```java
private final ArmFeedforward feedforward = new ArmFeedforward(ArmConstants.KS, ArmConstants.KG, ArmConstants.KV);
```
```java
private double gravityVolts() {
	return feedforward.calculate(getAngle().in(Radians), 0);
}
```
`calculate` takes the arm's angle in radians and the speed we want it to move at. We only want to hold it up, so the speed is 0. It works out `KG × cos(angle)`, the same math you wrote yourself, plus the `KS` and `KV` parts, which are 0 here. (`ArmFeedforward` is in `org.wpilib.math.controller`.)
Change `idle()` and `manualControl` to use `gravityVolts()` in place of `gravityThrottle()`. Run the simulator and everything should work as before. We changed *how* we talk to the motor, not what the arm does.

{{#quiz ../quizzes/ch02-07-volts.toml}}

## Pushing harder when you're farther away
Now for the real fix. How far the arm still has to go is called the **error**:
```
error = target angle - current angle
```
With the arm at 30 degrees and the target at 80, the error is 50. At 79, it's 1. At 85, it's -5: the arm went too far and needs to come back down.
The idea is to push in proportion to the error. Far away, push hard. Nearly there, push gently. Overshot, push back the other way.
```
volts = KP × error
```
`KP` is the number of volts to push per degree of error. With `KP` at 0.3, being 10 degrees away means 3 volts, and 1 degree away means 0.3 volts. As the arm closes in, the error shrinks, the push shrinks with it, and the arm slows down on its own. This is **proportional control**, or "P" for short.

You could write this yourself, but WPILib has a class that does it and more, `PIDController`. Add its numbers to `ArmConstants`:
```java
public static final double KP = 0.05;
public static final double KI = 0;
public static final double KD = 0;
```
And add a `PIDController` to `Arm`, next to the feedforward:
```java
private final PIDController pid = new PIDController(ArmConstants.KP, ArmConstants.KI, ArmConstants.KD);
```
(`PIDController` is also in `org.wpilib.math.controller`.) `moveTo` becomes:
```java
public Command moveTo(ArmPosition position) {
	return run(coroutine -> {
		pid.reset();
		while (!isAt(position)) {
			double pidVolts = pid.calculate(getAngle().in(Degrees), position.getAngle().in(Degrees));
			setVoltage(pidVolts + gravityVolts());
			Telemetry.log("Arm/Target", position.getAngle().in(Degrees));
			coroutine.yield();
		}
	}).named("Arm.MoveTo[" + position + "]").withTimeout(ArmConstants.MOVE_TIMEOUT);
}
```
- `pid.calculate(current, target)` works out the error and returns how many volts to push. Both angles go in as degrees, so `KP` is in volts per degree.
- `gravityVolts()` is added on, so P doesn't have to fight gravity alone. Feedforward and feedback work as a team: feedforward handles what can be worked out in advance, and feedback corrects whatever's left.
- `setVoltage` still clamps the result, so when the arm is far away it pushes at `MAX_VOLTAGE` as before. The difference shows near the end.
- `pid.reset()` clears anything `pid` remembers from its last use. (The "I" and "D" parts, coming up next, both keep track of things.)
- The loop logs the target too, so it can be graphed next to the arm's angle.
- The `setThrottle(0)` after the loop is gone. When `moveTo` finishes, `idle()` takes over and holds the arm up.

### P, I, and D
"PID" names the three parts a `PIDController` can add together:
- **P (proportional)** pushes in proportion to the error. It's the main part, and the one described above.
- **I (integral)** adds up the error over time. If the arm sits slightly short of the target for a while, the I part keeps growing until it's strong enough to push the rest of the way. Arms with good feedforward rarely need it, so `KI` is usually 0.
- **D (derivative)** looks at how fast the error is changing. When the error is shrinking fast, the arm is rushing at the target, so D pushes back a little, like brakes. It helps keep an arm from overshooting.

`KP`, `KI` and `KD` are called **gains**. Finding good gains is called **tuning**, which you're about to do.

### Watching it
Run the simulator, connect AdvantageScope, and drag both **Arm/Angle** and **Arm/Target** onto the graph. Then press the button for the scoring position.
The arm starts up quickly, then slows more and more as it gets close, and never quite makes it. After 3 seconds the timeout cancels `moveTo` with the arm about 9 degrees short. `KP` is far too small. At 10 degrees away, it pushes with only half a volt.
That's intentional. When tuning a real mechanism, you always start with a gain that's too small, because one that's too big can slam the mechanism into something. Then you raise it a little at a time and watch what happens. That's the exercise at the end of this lesson.

{{#quiz ../quizzes/ch02-07-pid.toml}}

## The real arm
On the real robot you'd tune the gains the same way, starting from the numbers you found in the simulator. They won't be exactly right, since no simulation is perfect. A real arm has friction, for one, and that's what `KS` is for.
Plenty of robot code doesn't use a `PIDController` at all. Most motor controllers, like REV's SPARK MAX, can run PID *themselves*, on the motor controller, about a thousand times a second. That's much faster than our robot code's 50 times a second, so it can control a mechanism even more smoothly. The idea is unchanged: you give the motor controller a target and some gains, and it pushes in proportion to the error. The gains go into the motor controller's settings instead of a `PIDController`.

Real arms often take one more step. Even with PID, our arm starts out at full speed with a jolt. A **motion profile** plans a smooth trip in advance: speed up gently, cruise, then slow down gently. PID then follows the plan instead of chasing a target that jumped all at once. WPILib has motion profiles too, and [What's Next](ch02-14-whats-next.md) says more about them.

## Try it yourself
Tune `KP`. Find a value that gets the arm from stowed to scoring in under 1.5 seconds. AdvantageScope's graph shows the timing: the bottom of the graph is time in seconds.

The usual approach:
1. Double `KP` and try the move again.
2. Look at the graph. Did it arrive? How long did it take? Did it go *past* the target line before settling?
3. Keep doubling until it's fast enough. If it ever starts going past the target and bouncing back, you've gone too far. Back off a bit, or try a small `KD`.

Each time, also try moving back down to the intake position, since gravity helps on the way down.

*Hint: every time you change a constant, you have to stop and restart the simulator for the change to take effect.*

<details>
<summary>Answer</summary>

Something close to this works well:
```java
public static final double KP = 0.3;
```
With `KP` at 0.3, the arm gets from stowed to scoring in about 1.2 seconds and arrives moving about 9 degrees a second instead of 85. On the graph, the top of the line is now a smooth curve instead of a sharp corner.
- At 0.1 it gets there, but it takes over 2.5 seconds.
- At 0.2 it takes about 1.5 seconds.
- Bigger values arrive a little sooner, but faster, so the stop is harder. At 1.0 it's almost the same as the old full-speed `moveTo`.

Even big values of `KP` don't make *this* arm overshoot. With 200-to-1 gearing, the motor spins 200 times faster than the arm, and a spinning motor resists being spun, almost like a brake. So as soon as the push drops, the arm slows right down and never builds up enough speed to sail past. That's why it needs no `KD`. A lighter, faster mechanism would, and that's when you'd reach for D.
If your number is a bit different, that's fine, as long as the arm arrives in time without bouncing. That's the nature of tuning. There's no single right answer, only numbers that work well enough.

</details>
