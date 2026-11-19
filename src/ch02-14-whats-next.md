# What's Next

You've built a complete robot program: an arm with feedforward and PID, an intake with a sensor, a swerve drivetrain you can drive around a field, teleop controls for two drivers, autonomous routines that use all of it together, and a simulation you can watch on a dashboard. You've also learned how to find bugs, run code safely on a real robot, and work on code with a team. Our real robot code has the same structure. It just has more mechanisms, with more commands for each.

These are the next things to learn, roughly in the order you'll need them.

## Our team's code
The best next step is reading our real robot code. Ask a mentor or a returning programmer where to find it. Much of it should look familiar now: mechanisms that implement `Mechanism`, IO interfaces with real and simulated versions, constants classes, and opmodes full of trigger bindings. When you hit something you don't recognize, look it up or ask. Reading code that works is one of the fastest ways to get better at writing it.

## Motion profiles
In [Feedback Control](ch02-07-feedback-control.md), PID chases the target as soon as it changes, so the mechanism starts with a jolt. A **motion profile** plans the whole move in advance: speed up gently, cruise, then slow down gently, never going faster or accelerating harder than you allow. PID then follows the plan step by step. WPILib's `TrapezoidProfile` makes the plan, and `ProfiledPIDController` is a `PIDController` that follows one for you. Most arms and elevators on real robots use one.

## Finding the gains
We found `KG` and `KP` by trying numbers and watching the results. That works, but there's a better way to find feedforward gains. **SysId** is a WPILib tool that runs a mechanism through a few test moves, records exactly how it responds, and calculates `KS`, `KV` and `KG` from the data. This is called **characterization**, and it gets you much closer to good gains before you start tuning by hand.

## Following paths
Our `driveTo` drives in a straight line to one spot. Real autonomous routines follow smooth curves around obstacles, often doing other things on the way. Teams plan these **paths** in an app like **PathPlanner** or **Choreo** by drawing them on a map of the field. A library in the robot code then follows the path, using PID controllers much like the ones in `driveTo` to stay on it.

## Vision
[Driving](ch02-09-driving.md) explained that a real robot's odometry slowly drifts from where the robot really is. Cameras correct that. The field has **AprilTags** on it, black-and-white squares like simple QR codes, at known spots. A camera running **PhotonVision** spots the tags and works out where the robot must be to see them the way it does. WPILib's **pose estimator** classes blend that with odometry, so the robot keeps a good idea of where it is. Our robot has cameras for exactly this, and the team has notes on setting them up.

## Testing your code
The simulator lets you test code by trying it out. **Unit tests** go a step further. They're small programs that check your code automatically, like the `Game.java` checkers in the first part of this book. A test might make an `Arm` with a pretend IO, tell it to move, and check that it arrived. Tests run on every build, including every pull request, so if someone breaks something later, the tests catch it right away. WPILib projects come set up for tests written with a library called **JUnit**.

## Logging
We used `Telemetry.log` to send a couple of numbers to AdvantageScope. Real robots send *hundreds*, so that when something goes wrong in a match, you can go back and see exactly what happened. WPILib has a tool called **Epilogue** that logs every field in a class automatically when you put `@Logged` above it.

## Where to look things up
The WPILib documentation at [docs.wpilib.org](https://docs.wpilib.org) covers everything in WPILib, with examples. WPILib also includes dozens of complete example projects. When you make a new project, choose **Example** instead of **Template** as the project type.
WPILib changed a lot in 2027. If code you find online imports anything starting with `edu.wpi.first`, it was written for an older version, and many of the names have changed. If it uses `SubsystemBase` or `CommandScheduler`, it's using the older command framework, Commands v2. WPILib still includes it, but it works differently from the Commands v3 we use, so its examples won't fit our code.

Finally, you know more than you think. All robot code is built from the things this book covered: classes, fields, methods, `if`s, loops, interfaces, lambdas and enums. When something looks confusing, break it into those pieces and read it one piece at a time.
