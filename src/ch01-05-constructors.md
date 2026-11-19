# Constructors

A new `Player` should probably start with full health. Instead of setting `health` by hand every time, we can have the class set it whenever a `Player` is built. That's the job of a **constructor**: code that runs when you use `new`.
```java
class Player {
	int health;

	Player() {
		health = 100;
	}
}
```
The constructor is the new part, `Player() { ... }`. Its pieces:

| `Player`                                          | `()`                  | `{` ... `}`                                    |
| ------------------------------------------------- | --------------------- | ---------------------------------------------- |
| Name of the class (it **must** match exactly) | Inputs (none for now) | The code that runs when the object is built |

Inside the constructor, `health` means the health of the `Player` being built.
```java
Player bob = new Player();
System.out.println(bob.health);
```
```
100
```
`bob.health` is already 100 without us setting it.

But we've been writing `new Player()` all along, with no constructor. How did that work?
When a class has no constructor, Java adds an empty one for you behind the scenes. Once you write your own, Java uses yours instead.

Not every `Player` should start with 100 health, though.
Back in [Classes and Objects](ch01-03-classes-and-objects.md), we said something would go in the `()` eventually, and this is it. The `()` is how you hand information to a constructor. These inputs are called **parameters**.
```java
class Player {
	int health;

	Player(int startingHealth) {
		health = startingHealth;
	}
}
```
`int startingHealth` is a variable: a type and a name, like in [Variables](ch01-02-variables.md). Its value is whatever you put in the `()` when you use `new`:
```java
Player bob = new Player(100);
Player alice = new Player(50);
System.out.println("bob: " + bob.health);
System.out.println("alice: " + alice.health);
```
```
bob: 100
alice: 50
```
The `100` goes into `startingHealth`, and the constructor copies it into `bob`'s `health`. `alice` gets `50` the same way.

There's one catch. This constructor needs a number, so `new Player()` with an empty `()` now gets a red squiggly line. Java only adds the free empty constructor to classes that don't have one of their own.

{{#quiz ../quizzes/ch01-05-constructors.toml}}

