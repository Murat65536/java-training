# Methods

Our `Player` can *have* things (fields), but it can't *do* anything yet.
Say `bob` gets hit for 30 damage. With what we know so far, we'd write:
```java
bob.health = bob.health - 30;
```
Health *equals* health minus 30? In math that would be nonsense.
In Java, `=` doesn't mean "these are equal." It means "set the left side to whatever the right side is." Java works out the right side first (`100 - 30` is `70`) and then puts the answer into `bob.health`.

That works, but it means writing the same line every time anyone gets hit. It's better to teach `Player` how to take damage on its own. That's what a **method** is for: a named chunk of code that belongs to a class.
```java
class Player {
	int health;

	Player(int startingHealth) {
		health = startingHealth;
	}

	void takeDamage(int amount) {
		health = health - amount;
	}
}
```
The parts of the new method:

| `void`                                    | `takeDamage`       | `(int amount)`                       | `{` ... `}`                          |
| ----------------------------------------- | ------------------ | ------------------------------------ | ------------------------------------ |
| What it gives back (`void` means nothing) | Name of the method[^1] | Parameters, just like a constructor | The code that runs when it's used |

A method looks a lot like a constructor, with two differences:
1. A method has its own name. A constructor's name is always the class name.
2. A method has a type in front of it (`void` here). A constructor never does.

You use a method the way you use a field, with a `.` to reach inside the object:
```java
Player bob = new Player(100);
bob.takeDamage(30);
```

| `bob`                  | `.`           | `takeDamage`       | `(30)`                | `;` |
| ---------------------- | ------------- | ------------------ | --------------------- | --- |
| The object we're using | "Look inside" | Name of the method | The value for `amount` | End |

Using a method like this is called **calling** it. To check that it worked:
```java
Player bob = new Player(100);
bob.takeDamage(30);
System.out.println(bob.health);
```
```
70
```
Each object keeps track of its own health, the same as with fields:
```java
Player bob = new Player(100);
Player alice = new Player(100);
bob.takeDamage(30);
System.out.println("bob: " + bob.health);
System.out.println("alice: " + alice.health);
```
```
bob: 70
alice: 100
```
Inside a method, `health` means the health of whichever `Player` the method was called on.

## Back to the template
With methods in hand, look at the template from [Your First Program](ch01-01-first-program.md) again:
```java
class Game {
	public static void main(String[] args) {
		// Your code goes here!
	}
}
```
`main` is a method. It lives inside a class (`Game`), and it has a type in front, a name, and a parameter in the `()`:

| `public static`                     | `void`             | `main`                                   | `(String[] args)`                                                                                              |
| ----------------------------------- | ------------------ | ---------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| Two more words we'll explain later | Gives nothing back | The name of the method | A parameter named `args`. Its type, `String[]`, holds a group of Strings. You won't need it for a while. |

What makes `main` special is its name. When you click **Run**, Java looks for the method named `main` and calls it.

`System.out.println(...)` follows the same pattern as `bob.takeDamage(30)`:
- `System.out` is an object that Java gives you. It stands for the place your program prints to.
- `.println` calls one of its methods.
- Whatever is in the `()` is the value for its parameter.

So every time you printed something, you were calling a method. The parts of the template still left to explain are `public static` and the `[]` in `String[]`. They come later.

{{#quiz ../quizzes/ch01-06-methods-calling.toml}}

## Giving something back
`void` means a method doesn't give anything back. It does its job and that's it. Sometimes, though, we want an answer.
Suppose we want to know how much health a `Player` is missing. A `Player` would need to remember how much health it started with, so we add a second field:
```java
class Player {
	int health;
	int maxHealth;

	Player(int startingHealth) {
		health = startingHealth;
		maxHealth = startingHealth;
	}

	void takeDamage(int amount) {
		health = health - amount;
	}

	int missingHealth() {
		return maxHealth - health;
	}
}
```
This time the type in front is `int`. That's a promise: "call this and you'll get an `int` back."
`return` keeps the promise. It hands the value back, and the method stops right there.
```java
Player bob = new Player(100);
bob.takeDamage(30);
int missing = bob.missingHealth();
System.out.println(missing);
```
```
30
```
When Java reaches `bob.missingHealth()`, it runs the method and puts whatever it returned in its place. The line ends up working like `int missing = 30;`.

A method that promises an `int` has to deliver one. Leaving out the `return`, or writing `return 3.9;`, gets a red squiggly line, for the same reason `3.9` can't go in an `int` variable.

{{#quiz ../quizzes/ch01-06-methods-return.toml}}

What happens here?
```java
Player bob = new Player(100);
bob.takeDamage(500);
System.out.println(bob.health);
```
```
-400
```
`bob` has -400 health, which doesn't make much sense. `takeDamage` should stop at 0. Right now, though, our code runs every line top to bottom no matter what. Fixing this means our code has to make decisions.



[^1]: Class names start with a capital letter (`Player`). Method and variable names start lowercase, and each new word gets a capital (`takeDamage`, `maxHealth`). Java doesn't enforce this, but everyone follows it, so code that breaks it looks strange to other programmers.
