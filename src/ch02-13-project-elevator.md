# Project: An Elevator

Until now, every lesson has told you what to type. This one doesn't. It's a project: you'll add a whole new mechanism to `ArmRobot` on your own, using everything you've learned, the way you'd add one to the real robot.
Many robots have an **elevator**, a carriage that slides straight up and down on rails, pulled by a motor that winds a belt or chain around a spinning **drum**. Ours will lift the arm so the robot can score higher.

## What to build
Work through the steps in order, and test each one in the simulator before moving on. Each step has a hint you can open if you get stuck, but try it yourself first.

### 1. Constants
Make `ElevatorConstants`, using units wherever they apply. The elevator is driven by **two NEOs** through a gear ratio of **10**, with a drum of radius **2.5 centimeters**. The carriage weighs **5 kilograms**. It travels from **0** to **1.2 meters** high. You'll add more constants as you go.

<details>
<summary>Hint</summary>

Heights are `Distance`s, like `Meters.of(1.2)`. `Centimeters` is a unit as well. A mass is a `Mass`, like the arm's.

</details>

### 2. The IO
Make `ElevatorIO` and `SimElevatorIO`. The IO should report the elevator's height in meters, take a voltage, and update.
For the simulator, WPILib has `ElevatorSim` in `org.wpilib.simulation`. It works much like `SingleJointedArmSim`. Its constructor takes, in order: the motors (as a `DCMotor`), the gear ratio, the carriage's mass in kilograms, the drum's radius in meters, the lowest and highest heights in meters, whether to simulate gravity, and the starting height in meters. Its `getPosition()` returns the height in meters.

<details>
<summary>Hint</summary>

`DCMotor.getNEO(2)` is two NEOs. Look back at `SimArmIO` in [Feedback Control](ch02-07-feedback-control.md). `SimElevatorIO` will look almost the same, with `setInputVoltage` and `update(OpModeRobot.DEFAULT_PERIOD)`.

</details>

### 3. The mechanism
Make `Elevator`, which `implements Mechanism`. It needs:
- A `getHeight()` that returns a `Distance`, and an `update()` that updates the IO and logs the height.
- A `private` `setVoltage` that clamps to a `MAX_VOLTAGE` and has soft limits, like the arm's.
- An `ElevatorPosition` enum with `BOTTOM`, `MIDDLE` and `TOP` heights. Pick heights between 0 and 1.2 meters.
- An `idle()` that holds the elevator still at the lowest priority.
- A `moveTo(ElevatorPosition)` that uses PID and feedforward, finishes when it's close enough, and has a timeout.

Then add it to `Robot` and update it in `robotPeriodic()`.

<details>
<summary>Hint</summary>

WPILib also has an `ElevatorFeedforward`, with `KS`, `KG` and `KV`. Gravity on an elevator doesn't change with height the way it does on an arm, so its `calculate` only needs the speed you want. `feedforward.calculate(0)` returns the volts needed to hold it still. Tune `KG` first, the same way as for the arm: start tiny and raise it until the elevator holds still when you let go. (It should end up around a quarter of a volt.) Then tune `KP`.

</details>

### 4. Drawing it
Add the elevator to the arm's `Mechanism2d` drawing so you can watch the arm ride up and down on it. The elevator should be a ligament pointing straight up from the root, with a *length* equal to the elevator's height. The arm's ligament should attach to the *end* of the elevator's ligament instead of the root.

<details>
<summary>Hint</summary>

`append` returns the ligament it attached, and you can call `append` on a ligament too, to attach another ligament to its end. Ligaments have a `setLength` method alongside `setAngle`. The arm's angle is measured from the ligament it's attached to, so if the elevator points up at 90 degrees, subtract 90 from the arm's angle when you set it.
The drawing now needs both mechanisms, so it no longer belongs in `Arm`. Move it into `Robot` and update it in `robotPeriodic()`.

</details>

### 5. Controls
Give the operator control of the elevator. The D-pad picks a position (up for `TOP`, left or right for `MIDDLE`, down for `BOTTOM`), and the right stick moves it by hand when nothing else is using it.

### 6. Autonomous
Change **Drive and Score** so the elevator rises to `TOP` while the robot drives and raises the arm, all three at once, and comes back down to `BOTTOM` when the arm stows.

### 7. A pull request
If you did [Working Together](ch02-12-working-together.md), do this whole project on its own branch, committing after each step that works. When you finish, open a pull request and ask a teammate to review it using that lesson's checklist.

## When you're done
Look at what you built from scratch: constants with units, an IO interface with a simulated version, a mechanism with feedforward and PID, a drawing, controls and an autonomous routine. That's every piece of a real mechanism on a real robot. The only part the real one still needs is a `RealElevatorIO`, and you know how to write that as well.

## Going further
For more, here are some ideas in no particular order:
- **Protect the arm.** On many robots, the arm can hit something if it swings down while the elevator is low. Make the arm refuse to go below the intake position unless the elevator is above a certain height. Where should that check go so that *no* command can get around it?
- **A score button.** Make a single operator button that raises the elevator and arm to their scoring positions at the same time, waits until both are there, and ejects.
- **Slow down near the ends.** Make `manualControl` move the elevator more slowly near the top or bottom, so the operator can't slam it into a soft limit.
- **Faster pickup.** Change `pickUp` so the arm starts back up as soon as the beam break sees a game piece, instead of waiting for the arm to reach the intake position first.
