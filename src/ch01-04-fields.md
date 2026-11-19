# Fields

`Player` is still empty.
Anything between a class's `{` and `}` belongs to it, and one thing that can go there is a variable. Here's `Player` with a `health` variable:
```java
class Player {
	int health;
}
```
A variable that belongs to a class like this is called a **field**[^1]. This one says that *every* `Player` has a `health`.

To use it, build a `Player` and use a `.` to reach inside it:
```java
Player bob = new Player();
bob.health = 100;
```

| `bob`                   | `.`          | `health`  | `=`        | `100` | `;` |
| ----------------------- | ------------ | --------- | ---------- | ----- | --- |
| The object we're using | "Look inside" | The field | Assignment | Value | End |

With two players, it gets more interesting:
```java
Player bob = new Player();
Player alice = new Player();
bob.health = 100;
alice.health = 50;
System.out.println("bob: " + bob.health);
System.out.println("alice: " + alice.health);
```
```
bob: 100
alice: 50
```
Each `Player` has *its own* `health`, so changing one leaves the other alone.
That's what it means for a class to contain a variable. The class says "every `Player` has a `health`," and each `Player` you build gets a separate copy.

You can read a field like any other variable:
```java
int total = bob.health + alice.health;
System.out.println(total);
```
```
150
```

If you never set a field, it still has a value:
```java
Player p = new Player();
System.out.println(p.health);
```
```
0
```
Number fields start at `0`, so a brand new `Player` has `0` health. That's a problem. We'd have to remember to write something like `bob.health = 100;` every time we make a `Player`, and sooner or later someone would forget.
Constructors solve this.

{{#quiz ../quizzes/ch01-04-fields.toml}}

[^1]: Fields are sometimes called *instance variables*, which means the same thing. Don't confuse them with *class variables*, which are something else that we'll get to later.
