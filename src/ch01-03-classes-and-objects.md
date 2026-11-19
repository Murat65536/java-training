# Classes and Objects

A class is like a blueprint. It describes what some kind of thing is and what it holds. Java code is built out of classes, so you'll see them everywhere.
Here's a class with nothing in it yet:
```java
class Player {
}
```
This class is named `Player`. Whatever goes between its `{` and `}` belongs to it. Ours is empty for now, and we'll start filling it soon.

The word `class` might look familiar. The template from [Your First Program](ch01-01-first-program.md) starts with `class Game {`, so `Game` is a class too. That's one piece of the template explained.

To try out `Player`, give it its own file. Every class goes in a file with exactly the same name as the class, plus `.java` on the end. Make a new file named `Player.java` in your `java-practice` folder, the same way you made `Game.java`, and put `Player` in it.

`Player.java`:
```java
class Player {
}
```
`Game.java` doesn't change:
```java
class Game {
	public static void main(String[] args) {
		// Your code goes here!
	}
}
```
Your folder now has two files. When you click **Run**, VS Code builds every `.java` file in the folder together, so the code in `Game`'s `main` can use `Player` even though `Player` lives in another file. Your code still goes in `main`, in `Game.java`.
(Java doesn't require one class per file. A file can hold more than one class. But with one class per file, you can find any class by its name. Robot code is organized this way, so we'll do the same from the start.)

A blueprint on its own doesn't do anything. You can't live in the blueprint of a house. Someone has to build the house first. Building something from a class is called making an **instance**[^1] of it, also called an **object**. You do it with `new`:
```java
new Player();
```
Here's what each part means:

| `new`                                                    | `Player`          | `()`                                                          | `;`                                |
| -------------------------------------------------------- | ----------------- | ------------------------------------------------------------- | ---------------------------------- |
| The indicator that you are making an instance of a class | Name of the class | Always needed when building. We'll put things in here later. | Indicates the end of the statement |

There's a problem with that line, though. As soon as it finishes, the new `Player` is gone, because nothing is holding on to it.
Holding on to things is what variables are for. They can hold objects as well as primitives:
```java
Player bob = new Player();
```

| `Player` | `bob` | `=`        | `new Player()` | `;` |
| -------- | ----- | ---------- | -------------- | --- |
| Type     | Name  | Assignment | Value          | End |

That's the same pattern as `int v = 42;` from [Variables](ch01-02-variables.md). The type is our class, `Player`, and the value is a brand new `Player`.
Here Java variables start to differ from the ones in math class. A variable can hold a whole *thing*, not only a number.

Put `Player bob = new Player();` inside `main` and click **Run**. Nothing prints, since nothing asked to print. If there are no red squiggly lines, though, you've built your first object.

The last lesson said `String` isn't a primitive, which is why it has a capital `S`. `String` is a class, like `Player`, and it comes built into Java. So `String name = "Sam";` is a variable holding an object, the same as `Player bob = new Player();`.
Strings are used so much that Java gives you a shortcut: instead of `new`, you write the text in double quotes.

A class is a blueprint, so you can build as many objects from it as you like:
```java
Player bob = new Player();
Player alice = new Player();
```
Those are two separate `Player`s built from the same blueprint. They're pretty boring with nothing inside them. The next lesson fixes that.

{{#quiz ../quizzes/ch01-03-classes-and-objects.toml}}

[^1]: Making an instance of a class means "building" a new object from the "blueprint."
