# Objects Working Together

## Players attacking players
So far a `Player` only gets hurt when *we* call `takeDamage` on them. The game gets more interesting if players can attack each other.
An attacker has to know *who* they're attacking, so `attack` gets a parameter, like `takeDamage` has. This time the parameter is a `Player`:
```java
void attack(Player target) {
	if (isAlive()) {
		target.takeDamage(10);
	}
}
```

| `void`             | `attack`           | `(Player target)`                                     |
| ------------------ | ------------------ | ----------------------------------------------------- |
| Gives nothing back | Name of the method | A parameter named `target`, whose type is `Player` |

A parameter can be any type, including classes we wrote ourselves. Inside `attack`, `target` is the `Player` being attacked, so `target.takeDamage(10);` hurts *them*.
Compare the two calls in there:
- `isAlive()`, with nothing in front, means the `Player` doing the attacking. Dead players can't attack.
- `target.takeDamage(10)` means the `Player` being attacked.

Trying it out:
```java
Player bob = new Player(100);
Player alice = new Player(100);
bob.attack(alice);
System.out.println("bob: " + bob.health);
System.out.println("alice: " + alice.health);
```
```
bob: 100
alice: 90
```
`bob` attacked and `alice` took 10 damage. `bob` wasn't touched.

{{#quiz ../quizzes/ch01-09-attack.toml}}

## Same object, two names
Something odd happened there. Inside `attack`, the code hurt `target`, not `alice`. So how did `alice` end up with 90 health?
A simpler example helps. Start with `int`s:
```java
int v = 42;
int g = v;
g = g + 10;
System.out.println("v: " + v);
System.out.println("g: " + g);
```
```
v: 42
g: 52
```
Nothing surprising. `int g = v;` copies 42 into `g`. From then on `g` and `v` are separate, so changing `g` leaves `v` alone.
Now the same thing with a `Player`:
```java
Player bob = new Player(100);
Player sam = bob;
sam.takeDamage(30);
System.out.println("bob: " + bob.health);
System.out.println("sam: " + sam.health);
```
```
bob: 70
sam: 70
```
Only `sam` took damage, yet `bob` got hurt too.

Go back to the blueprint picture, where a class is a blueprint and an object is a house built from it. A variable doesn't hold the house. It holds the house's **address**, like a slip of paper that says where the house is.
([Classes and Objects](ch01-03-classes-and-objects.md) said a variable can hold a whole thing. That was close enough at the time, but this is what really happens.)
- `new Player(100)` builds a house.
- `Player bob = ...` writes that house's address on a slip of paper named `bob`.
- `Player sam = bob;` copies the address onto a second slip named `sam`. It does *not* build a second house.

`bob` and `sam` are two names for the **same** `Player`. Whatever you do through one name shows up through the other.

Counting `new`s is a useful trick. Each `new` builds exactly one object. The code above has one `new`, so it has one `Player`, however many variables point at it. This version has two `new`s, so it has two separate `Player`s:
```java
Player bob = new Player(100);
Player sam = new Player(100);
sam.takeDamage(30);
System.out.println("bob: " + bob.health);
```
```
bob: 100
```

`attack` works the same way. When we call `bob.attack(alice)`, Java copies `alice`'s address into `target`. Inside `attack`, `target` is another name for `alice`, so hurting `target` hurts `alice`.

(This is also why `int`s and `Player`s behave differently. A primitive like `int` is simple enough that the variable holds the value itself, so copying it makes a real copy. Objects are always reached through an address.)

Robot code relies on this constantly. A robot has one drivetrain, but many parts of the code need to drive it. Each of them is handed the address of that *same* drivetrain object, the way `target` is handed `alice`.

{{#quiz ../quizzes/ch01-09-same-object.toml}}

## The same object, or just the same?
Addresses also change what `==` means. For `int`s, `==` asks "are these the same number?" For objects, `==` compares *addresses*, so it asks "are these the same house?"
```java
Player bob = new Player(100);
Player sam = bob;
Player amy = new Player(100);
System.out.println(bob == sam);
System.out.println(bob == amy);
```
```
true
false
```
`bob` and `sam` are two names for one `Player`, so `bob == sam` is `true`. `amy` has the same health as `bob`, but `amy` is a separate `Player` from a separate `new`, so `bob == amy` is `false`.

This matters most for `String`s, since a `String` is an object too. Two `String`s can have the same letters and still be different objects:
```java
String weapon = "Sword";
String start = "Sw";
String built = start + "ord";
System.out.println(weapon == built);
System.out.println(weapon.equals(built));
```
```
false
true
```
`built` holds the letters `Sword`, but `+` made a brand new `String` to hold them, so it isn't the same object as `weapon`. That's why `==` says they're different. Every `String` has an `equals` method that compares the letters, and it says they match.

> [!WARNING]
> Compare `String`s with `equals`, never `==`. What makes this tricky is that `==` *sometimes* works on `String`s. Java can reuse one object for text that's typed into the code the same way twice. So code using `==` can pass every test and then break later.

{{#quiz ../quizzes/ch01-09-equals.toml}}

## More than one parameter
Every attack does 10 damage right now. Some attacks should hit harder than others.
A method can take more than one parameter, separated by commas:
```java
void attack(Player target, int damage) {
	if (isAlive()) {
		target.takeDamage(damage);
	}
}
```
When you call it, give a value for each parameter, in the same order, also separated by commas:
```java
Player bob = new Player(100);
Player alice = new Player(100);
bob.attack(alice, 25);
alice.attack(bob, 5);
System.out.println("bob: " + bob.health);
System.out.println("alice: " + alice.health);
```
```
bob: 95
alice: 75
```
In `bob.attack(alice, 25)`, the first value (`alice`) goes into the first parameter (`target`), and the second value (`25`) goes into the second parameter (`damage`).
Order matters. `bob.attack(25, alice)` gets a red squiggly line, because Java would try to put `25` into `target`, and `25` isn't a `Player`.

Constructors can take more than one parameter in the same way.

### Same name, different parameters
Two methods in one class can share a name as long as their parameters are different. If most attacks do 10 damage, we could keep a one-parameter `attack` next to the new one:
```java
void attack(Player target) {
	attack(target, 10);
}

void attack(Player target, int damage) {
	if (isAlive()) {
		target.takeDamage(damage);
	}
}
```
Java decides which one to run by looking at what you pass in. `bob.attack(alice)` passes one value, so it runs the first `attack`, which calls the second one with 10. `bob.attack(alice, 25)` passes two, so it runs the second one directly.
This is called **overloading**, and you've used it since your first program. `System.out.println` is overloaded, with one version for a `String`, one for an `int`, one for a `boolean`, and more. WPILib uses it all over. Many of its methods come in two versions, one taking a plain number and one taking a measurement with units, which you'll see in [Units](ch02-03-units.md).
(The one-parameter `attack` won't stay in our game. It's only here to show what overloading looks like.)

## Putting it together
Here's the whole `Player` class now. Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-09}}

## Try it yourself
Players should be able to help each other too. Add a method `giveHealth` that takes a `Player` named `friend` and an `int` named `amount`. It should:
1. Take `amount` health away from the `Player` doing the giving.
2. Heal `friend` by `amount`.
3. Do nothing at all if the giver is dead.

*Hint: you don't have to write it all from scratch. `Player` already has methods that do most of the work.*

To check your answer, add `giveHealth` to `Player.java` and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-09/Game.java}}
```
If every line prints what its comment says, your `giveHealth` works.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-09}}
`takeDamage` and `heal` already change health safely, so `giveHealth` calls them. Reusing the methods you have means you don't write (and debug) the same code twice.

</details>
