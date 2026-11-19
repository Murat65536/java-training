# Finding Bugs

Every programmer writes bugs, constantly. Experienced programmers write them too. What they've gotten better at is *finding* them, and that's a skill you can build with practice and a good plan.
Bugs come in three kinds, and each needs its own plan:
1. The code won't **build**.
2. The code builds but **crashes** when it runs.
3. The code runs but **does the wrong thing**.

## When it won't build
You've seen plenty of these already, as red squiggly lines. When Java can't understand your code, it won't run any of it.
- **Hover over the squiggle** to see the message, then read it carefully, word by word. It usually says exactly what's wrong. `bonus cannot be resolved to a variable` means Java doesn't know of any variable named `bonus` at that spot. Maybe it's misspelled, or maybe it's out of scope.
- **Open the Problems panel** with **Ctrl+Shift+M** to see every error in the project at once. Click one to jump to it.
- **Fix the first one first.** One mistake can confuse Java about everything after it. A missing `}`, for example, can make the whole rest of the file look wrong. Fixing the first error often clears up a bunch of the others.
- **Try Quick Fix.** Click on the squiggle and press **Ctrl+.** to see what VS Code suggests. It's great for missing imports. Read what it offers before you accept, though, because it doesn't always guess right.

A few robot code errors only show up when you build, not as squiggles. To build without running, open the command palette, pick `WPILib: Build Robot Code`, and watch the terminal. The `coroutine.yield()` check from [Loops](ch01-18-loops.md) is one of these, and so is the integer division check from [Decimals and Math](ch01-14-decimals-and-math.md#dividing).
Some messages you'll see often, and what they usually mean:

| Message | What it usually means |
| ------- | --------------------- |
| `... cannot be resolved to a type` | A class is missing its `import`, or its name is misspelled. |
| `Syntax error, insert ";" to complete Statement` | A missing `;`, usually at the end of the line *before* the squiggle. |
| `Type mismatch: cannot convert from NeedsNameBuilderStage to Command` | A command is missing its `.named(...)`. |
| ``Missing call to `coroutine.yield()` inside loop`` | A `while` loop in a command never yields. |
| `Integer division in a floating-point context.` | Two `int`s are divided and the answer goes into a `double`. Make one side a `double` first. |
| `The method ... is undefined for the type ...` | A method name is misspelled, or you're calling it on the wrong kind of object. |

{{#quiz ../quizzes/ch02-10-build.toml}}

## When it crashes
Sometimes code builds fine but hits something it can't do while it runs. The `NullPointerException` from [Objects Inside Objects](ch01-10-objects-inside-objects.md) was one of these crashes. When Java runs into a problem it can't get past, it **throws an exception**: it stops what it's doing and hands back an object describing what went wrong.
If nothing catches the exception, the whole program stops. On the real robot, that means the robot stops *completely*, possibly in the middle of a match, and its code takes several seconds to start back up. The Driver Station shows **No Robot Code** in the meantime. Every crash is worth fixing right away.

### Reading a stack trace
When a robot program crashes, WPILib prints what happened. Say you left off the `= new Drive(...)` part, so `Robot` only had `Drive drive;`. Here's the important part of what you'd see:
```
Unhandled exception: java.lang.NullPointerException: Cannot invoke "frc.robot.mechanisms.Drive.update()" because "this.drive" is null
	at frc.robot.Robot.robotPeriodic(Robot.java:31)
	at org.wpilib.framework.OpModeRobot.loopFunc(OpModeRobot.java:...)
	...
The robot program quit unexpectedly. This is usually due to a code error.
```
This is a **stack trace**. It looks intimidating, but only two parts matter:
- **The first line says what went wrong.** It's a `NullPointerException`, and Java spells out why: it tried to call `update()` on `this.drive`, and `drive` is `null`.
- **The `at` lines say where.** The top one is where it happened. Each line below it is the method that *called* the one above, all the way back to the program's start. Read down until you reach the first line in *our* code, which starts with `frc.robot`. Here that's the very first one, `Robot.java`, line 31. Click it in VS Code's terminal to jump straight there.

Lines from inside WPILib (starting with `org.wpilib`) or Java (starting with `java.`) are almost never where the bug is. They only show how the program got to our code.
When a command crashes, the scheduler adds a little extra to its stack trace: the line where the command was bound to a trigger or awaited. You can see both *where* the command crashed and *what started it*.

### Exceptions you'll meet
| Exception | What it means |
| --------- | ------------- |
| `NullPointerException` | You used an object variable that was `null`, usually because something was never made with `new`. |
| `ArrayIndexOutOfBoundsException`, `IndexOutOfBoundsException` | You asked for an index that isn't in the array or list, like `[4]` in a list of 4. (Remember, they start at 0!) |
| `ArithmeticException: / by zero` | Dividing an `int` by 0. (Dividing a `double` by 0 doesn't crash. It gives back `Infinity` or `NaN`, which is "not a number," and that can be an even sneakier bug.) |
| `IllegalArgumentException`, `IllegalStateException` | A method was handed something it can't work with, or was called at a time it can't run. Read the message: WPILib's are usually very specific. |

### Throwing and catching
Your own code can throw an exception too, with `throw`:
```java
public Weapon(String weaponName, int weaponDamage) {
	if (weaponDamage < 0) {
		throw new IllegalArgumentException("A weapon can't do negative damage");
	}
	name = weaponName;
	damage = weaponDamage;
}
```
That may seem like an odd thing to want. But crashing immediately with a clear message beats silently doing the wrong thing and finding out during a match.
Code can also **catch** an exception so it doesn't crash the program:
```java
try {
	int number = Integer.parseInt(text);
	System.out.println("You typed " + number);
} catch (NumberFormatException e) {
	System.out.println("That's not a number!");
}
```
`Integer.parseInt` turns text like `"42"` into the `int` 42, and throws a `NumberFormatException` if the text isn't a number. Java runs the `try` part, and if anything in it throws a `NumberFormatException`, it jumps straight to the `catch` part instead of crashing. `e` is the exception object, in case you need it.
You won't write `try` and `catch` very often in robot code. They're mostly for things that can fail even when the code is right, like reading a file that might not exist. Don't use them to hide a bug. If code is crashing, fix the code.

{{#quiz ../quizzes/ch02-10-crashes.toml}}

## When it does the wrong thing
These are the hardest bugs, because Java gives you no message at all. The code runs without complaint. It just doesn't do what you meant.

### Check the obvious first
Before digging into the code, make sure it really is a code problem. A surprising number of "bugs" turn out to be one of these:
- Is the robot **enabled**, in the right mode?
- Is the right **opmode** selected?
- Is the controller in the right **slot** in the Sim GUI, or plugged into the Driver Station?
- Did you **save** the file and restart the simulator (or deploy again) after changing it?
- Is it **the code you think it is**? If you have more than one copy of the project, check which one is open.

### Look at what's really happening
The most important rule for finding bugs is *don't guess, look.* It's tempting to stare at the code and imagine what it does. But a bug is exactly the place where the code does something different from what you imagine, so imagining won't find it.
- **Log it.** Add a `Telemetry.log` for whatever you're unsure of, and graph it in AdvantageScope. Is the arm's target what you think? Does the sensor really turn `true`? A graph often makes a bug obvious.
- **Print it.** `System.out.println` works in robot code and shows up in the terminal. It's handy for checking whether a line runs at all. Just don't print on every `robotPeriodic()`, or you'll get 50 lines a second and the one you need will scroll past too fast to read.
- **Change one thing at a time.** If you change three things and it starts working, you won't know which one fixed it. If it gets worse, you won't know which one broke it.

### The debugger
VS Code has a tool for looking inside running code, called the **debugger**. When you run the simulator from VS Code, the debugger is already connected.
1. Click just to the left of a line number. A red dot appears. That's a **breakpoint**.
2. Run the simulator. When the robot reaches that line, *everything stops*, and VS Code highlights the line.
3. Hover over any variable to see its current value. The **Variables** panel on the left shows all of them.
4. Use the buttons at the top of the window to continue. **Continue** (F5) runs until the next breakpoint. **Step Over** (F10) runs a single line, so you can follow the code one step at a time.

Click the red dot again to remove it.

> [!WARNING]
> A breakpoint freezes the *whole* robot program mid-update. That's fine in the simulator. Never use one on a real robot that can move. Frozen code can't stop a motor, so the motor keeps doing whatever it was last told.

### When the robot gets slow
Sometimes you'll see `Watchdog not fed within 0.020000s` in the Driver Station, or a `Loop time of 0.02s overrun` alert on your dashboard. Both mean an update took longer than 20 milliseconds, so the robot is falling behind. Something is taking too long: a loop in a command that forgot to yield and is doing a lot of work, too much printing, or waiting with `Thread.sleep`, which freezes the whole robot. (Never use `Thread.sleep` in robot code. Use `coroutine.wait` instead.)

## Asking for help
When you're stuck, ask. A mentor or teammate can often spot in a minute what you've stared at for an hour. You'll get help much faster if you bring:
1. **What you expected** to happen.
2. **What happened instead.** Copy the exact error message or stack trace rather than describing it.
3. **What you've already tried**, and what it showed.

Often, explaining the problem out loud makes you spot it yourself before the other person says a word. Programmers call this "rubber duck debugging," because it works even when you explain it to a rubber duck.

## Try it yourself
Each of these is a bug report from a teammate. Work out what's wrong in each and how to fix it.

1. "The arm only goes up while I'm *holding* the top face button. As soon as I let go, it stops." Here's the binding:
   ```java
   robot.operatorController.faceUp().whileTrue(robot.arm.moveTo(ArmPosition.SCORING));
   ```
2. "When I press the pickup button, the arm goes down and the rollers start, but then nothing else happens, ever. And **Intake/HasGamePiece** isn't on AdvantageScope's list at all." Here's `robotPeriodic()`:
   ```java
   @Override
   public void robotPeriodic() {
   	arm.update();
   	drive.update();
   	Scheduler.getDefault().run();
   }
   ```
3. "The simulator crashes as soon as it starts." Here's the important part of what it printed:
   ```
   Unhandled exception: java.lang.NullPointerException: Cannot invoke "frc.robot.mechanisms.ArmIO.getAngleDegrees()" because "this.io" is null
   	at frc.robot.mechanisms.Arm.getAngle(Arm.java:35)
   	at frc.robot.mechanisms.Arm.update(Arm.java:46)
   	at frc.robot.Robot.robotPeriodic(Robot.java:28)
   ```
   And here's the top of `Arm`:
   ```java
   private ArmIO io;

   public Arm(ArmIO armIO) {
   	ArmIO io = armIO;
   }
   ```

*Hint for the last one: think about [scope](ch01-07-if-statements.md#where-variables-live). How many different variables named `io` are there?*

<details>
<summary>Answer</summary>

1. `whileTrue` cancels the command as soon as the button is released. `moveTo` has an end, so it should use `onTrue`, which keeps it running until the arm arrives, even after the button comes up.
2. `robotPeriodic()` never calls `intake.update()`. The pretend intake never counts how long the rollers have run, so it never gets a game piece, and `intake()` waits forever. The clue was that **Intake/HasGamePiece** wasn't logged at all. It's logged in `Intake`'s `update`, so `update` was never running. Add `intake.update();` back.
3. `ArmIO io = armIO;` makes a brand-new *local* variable named `io` inside the constructor and stores the IO there. That variable disappears at the constructor's `}`. The *field* named `io` never gets set, so it's still `null` when `getAngle` uses it. The fix is to drop the `ArmIO` from the start of the line so it sets the field: `io = armIO;`. The stack trace pointed right at it: `Arm.getAngle`, at the line that uses `io`, called from `update`, called from `robotPeriodic`.
   Someone had also removed the `final` from the field. With `private final ArmIO io;`, Java wouldn't have built this at all. A `final` field has to be set in the constructor, so you'd have gotten a red squiggly line saying `The blank final field io may not have been initialized`. That's another reason to make fields `final` whenever you can: it turns a crash into a red squiggly line.

</details>
