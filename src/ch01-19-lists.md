# Lists

## Arrays
Every variable so far has held one thing: one number, one `Player`, one `Weapon`. Games are full of groups, though, like a team of players or every crate in the arena. Healing everyone on a team looks like this:
```java
bob.heal(Constants.HEALER_HEAL_AMOUNT);
alice.heal(Constants.HEALER_HEAL_AMOUNT);
carl.heal(Constants.HEALER_HEAL_AMOUNT);
```
Fine for three players, but not for thirty. We want to keep the whole team in *one* variable and do something to each member.
An **array** does that. It's a list of things of the same type, held in one variable:
```java
Player[] team = {bob, alice, carl};
```

| `Player[]`                 | `team`   | `=` | `{bob, alice, carl}`                     |
| -------------------------- | -------- | --- | ---------------------------------------- |
| The type: an array of `Player`s | The name |     | What's in it, in order, separated by commas |

`[]` after a type means "an array of these." `Player[]` is an array of `Player`s, and `int[]` would be an array of `int`s.

### Indexes
Each spot in an array has a number called its **index**. Put the index in `[ ]` to get that spot:
```java
team[1].takeDamage(30);
System.out.println("alice: " + alice.getHealth());
```
```
alice: 70
```
`team[1]` turned out to be `alice`, not `bob`. That's because indexes start at **0**, not 1. `bob` is `team[0]`, `alice` is `team[1]`, and `carl` is `team[2]`. It takes some getting used to, but nearly every programming language counts this way.
(`team[1]` and `alice` are also the same `Player`. The array holds each player's address, the way a variable does, so hurting `team[1]` hurts `alice`.)

`team.length` is the number of spots in the array, `3` here, so the last index is always `length - 1`, which is `2`. Ask for one past the end and the program crashes:
```java
System.out.println(team[3]);
```
```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3
```
An array's length never changes. Once it's made, it has the same number of spots forever.

### Going through an array
A `for` loop's `i` starts at 0, as [Loops](ch01-18-loops.md) showed, which makes it a perfect fit for going through every index:
```java
for (int i = 0; i < team.length; i++) {
	System.out.println("player " + i + ": " + team[i].getHealth());
}
```
```
player 0: 100
player 1: 70
player 2: 100
```
`i` goes 0, 1, 2 and stops at `team.length`. Writing `i < team.length` instead of `i < 3` means the loop works for a team of any size.

You'll see loops like that a lot. When you want to do something to *every* item and don't need the index, a **for-each loop** is simpler:
```java
for (Player player : team) {
	player.heal(Constants.HEALER_HEAL_AMOUNT);
}
```
Read the `:` as "in": "for each `Player`, called `player`, in `team`." The first time around, `player` is `bob`, then `alice`, then `carl`. There's no index to track and no way to run past the end.

### Arrays you've already seen
Every enum comes with an array of all its values, called `values()`:
```java
for (Stance stance : Stance.values()) {
	System.out.println(stance + " takes " + stance.getDamageMultiplier() + "x damage");
}
```
```
NORMAL takes 1.0x damage
DEFENDING takes 0.5x damage
AGGRESSIVE takes 1.5x damage
```
Add a fourth stance and this loop prints it too, with no changes.

The last unexplained piece of the template goes back to [Methods](ch01-06-methods.md):
```java
public static void main(String[] args) {
```
`args` is an array of `String`s. If you start your program from the terminal with extra words after it, like `java Game.java hello`, then `args[0]` is `"hello"`. VS Code's **Run** button doesn't pass any, so `args.length` is 0. With that, every part of the template has been explained.

{{#quiz ../quizzes/ch01-19-arrays.toml}}

## ArrayLists
An array's length never changes, which suits groups that stay one size. For a group that changes, like everything in the arena while new crates keep appearing, Java has the **`ArrayList`**, a list that can grow and shrink.
```java
ArrayList<Damageable> targets = new ArrayList<>();
```
The type inside `< >` says what the list holds, so this is a list of `Damageable`s. A new `ArrayList` starts out empty. (The `<>` after `new ArrayList` is empty because Java can tell it's the same type as on the left.)

An `ArrayList` is an object, unlike an array, so you work with it through methods:
```java
Crate crate = new Crate();
targets.add(alice);
targets.add(crate);
System.out.println(targets.size());
System.out.println(targets.get(1).getHealth());
```
```
2
40
```
- `add` puts something on the end of the list.
- `size()` is how many things are in the list right now, like an array's `length`.
- `get(1)` gets the thing at index 1. Indexes start at 0, as with arrays, so this is the crate.
- `remove(alice)` takes `alice` out of the list, and everything after it moves up one spot.
- `contains(alice)` tells you whether `alice` is in the list.

A for-each loop works on an `ArrayList` the same way it does on an array. Here's a `Player` method that attacks everything in a list:
```java
public void attackAll(ArrayList<Damageable> targets) {
	for (Damageable target : targets) {
		attack(target);
	}
}
```
```java
bob.attackAll(targets);
System.out.println("alice: " + alice.getHealth() + ", crate: " + crate.getHealth());
```
```
alice: 89, crate: 39
```
The list holds `Damageable`s, so players and crates can be mixed in it, the same way `attack` can hit either one.

### Importing
`ArrayList` needs one more thing before it works. Type the code above in and `ArrayList` gets a red squiggly line saying `ArrayList cannot be resolved to a type`.
Java comes with thousands of classes and sorts them into groups called **packages**. A few, like `String`, `Math` and `Runnable`, are so common you can always use them. Everything else needs an **`import`** at the very top of the file that says which package it's in. `attackAll` is in `Player.java`, so the import goes there:
```java
import java.util.ArrayList;

public class Player implements Damageable {
	// ...
}
```
`java.util` is the package, and `ArrayList` is the class in it. The `import` has to come before anything else in the file, `public class Player` included.
Each file imports what it uses. If `main` makes an `ArrayList` too, `Game.java` needs `import java.util.ArrayList;` at its top as well.
You don't have to memorize where things live. Hover over the red squiggly line, click **Quick Fix**, and VS Code offers to add the `import` for you.

### Your own packages
Your own classes can go in packages as well, and a package is just a folder. Say you made a folder named `constants` inside `java-practice` and moved `Constants.java` into it. Then the first line of `Constants.java` would have to name its package:
```java
package constants;

public final class Constants {
	// ...
}
```
Every file outside that folder that uses `Constants`, like `Player.java`, would need to import it, the same as `ArrayList`:
```java
import constants.Constants;
```
This also settles something from [Protecting Your Fields](ch01-12-protecting-your-fields.md). That lesson said that with no `public` or `private`, only code "in the same folder" can use something. The real name for that folder is the package. A class in the `constants` package can only be used from other packages if it's `public`, and the same goes for its fields.
One catch: code in a package can't use classes that aren't in *any* package, like `Player`. That's why every file in a real project is in a package. Our game is small enough to stay in one folder, so you don't need to do this. A robot project has dozens of files, though, so it's split into many packages, and every file starts with a `package` line.

Robot code files all start with a stack of `import`s, for things like `Mechanism` and `SparkMax`. Now you know what they do.

> [!WARNING]
> An `ArrayList` can only hold objects, not primitives like `int` or `double`. `ArrayList<int>` gets a red squiggly line. For a list of numbers, use the object version of the type, which starts with a capital letter: `ArrayList<Integer>` or `ArrayList<Double>`. You can still `add` plain numbers to them, and Java turns them into objects for you.

> [!WARNING]
> Don't add things to a list or remove things from it *while* a for-each loop is going through it. The program will crash with a `ConcurrentModificationException`. To take things out of a list, like everything that's been knocked out, use the list's `removeIf` method. It takes a lambda that says which things to remove:
> ```java
> targets.removeIf(target -> target.getHealth() == 0);
> ```
> (A lambda with exactly one parameter can leave out the `( )` around it.)

### List
Lists are often written like this:
```java
List<Player> players = new ArrayList<>();
```
`List` is an interface, and `ArrayList` implements it, the way `Crate` implements `Damageable`. Java has other kinds of lists that implement `List` too. Writing `List` on the left says "I need *a* list, and I don't care which kind," the same way `attack` takes any `Damageable`. (`List` also lives in `java.util`, so it needs its own `import java.util.List;`.)

{{#quiz ../quizzes/ch01-19-arraylists.toml}}

## Lists on a robot
[Loops](ch01-18-loops.md) said that a robot-code loop that *waits* has to call `coroutine.yield()` so the rest of the robot gets a turn. A loop over a list finishes right away, so it doesn't need to.
Take a swerve drivetrain. It has four wheels, and each wheel's assembly, called a **module**, can steer on its own. A robot always has exactly four, so an array fits well:
```java
public class Drivetrain implements Mechanism {
	private final SwerveModule[] modules;

	public Drivetrain(SwerveModule frontLeft, SwerveModule frontRight, SwerveModule backLeft, SwerveModule backRight) {
		modules = new SwerveModule[] {frontLeft, frontRight, backLeft, backRight};
	}

	public void stopAll() {
		for (SwerveModule module : modules) {
			module.stop();
		}
	}
}
```
(When you make an array anywhere other than the line where the variable is declared, you write `new SwerveModule[]` in front of the `{ }`.)
`stopAll` goes through the four modules, stops each one, and it's done. That takes a tiny fraction of a millisecond, so the rest of the robot never waits on it.
When the number of something on a robot really does change, like the game pieces the camera can see at the moment, that's a job for an `ArrayList`.

## Putting it together
Here's everything now, with the new `import` at the top of `Player.java`. Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-19}}

## Try it yourself
A smart fighter goes after whoever is easiest to knock out. Add a method `public void attackWeakest(ArrayList<Damageable> targets)` to `Player`. It should attack the target in the list with the least health, skipping anything already knocked out. If nothing is left to attack, or the list is empty, it does nothing.

*Hint: go through the list and keep the weakest target found so far in a variable. What should that variable start as, before you've looked at anything?*

To check your answer, make your changes and replace the `main` in `Game.java` with this one. It has an `import` at the top, because this `main` uses `ArrayList` too:
```java
{{#include ../code/ch01-19/Game.java}}
```
If every line prints what its comment says, your fighter picks the right target.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-19}}
`weakest` starts as `null`, because before looking at anything, no weakest target has been found. Then, for each target that isn't knocked out, the code asks two questions: is this the first one found (`weakest == null`), or is it weaker than the weakest so far? If either is true, it becomes the new weakest.
The order inside the `if` matters. With `||`, Java only checks `target.getHealth() < weakest.getHealth()` when `weakest == null` was `false`, so it never looks inside `weakest` while it's still `null`.
After the loop, `weakest` is still `null` if there was nothing to attack, so the code checks before attacking. Without that check, an empty list would crash with a `NullPointerException`.

</details>
