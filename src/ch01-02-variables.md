# Variables

A variable is a named place to keep a piece of information. Making one takes two things: a **type** and a **name**.
The type says what kind of value the variable can hold. Two types you'll use all the time:

|        | `int`         | `double`        |
| ------ | ------------- | --------------- |
| Stores | Whole numbers | Decimal numbers |

The name comes after the type, and it's whatever you want to call the variable.

Put those together and you get the simplest possible variable:
```java
int v;
```
The `;` at the end is a semicolon. Java uses it to mark the end of a piece of code, the way a period ends a sentence.

That variable doesn't hold anything useful yet. To give it a value, you **assign** one. An `int` holds whole numbers, so the value has to be a whole number. It looks a lot like math, where `x = 4` gives `x` the value `4`:
```java
int v = 42;
```
Here's what each part does:

| `int` | `v`  | `=`        | `42`  | `;` |
| ----- | ---- | ---------- | ----- | --- |
| Type  | Name | Assignment | Value | End |

This line makes an integer named `v` and gives it the value `42`.

A new variable can also get its value from another one. This makes `g` and sets it to `v`:
```java
int v = 42;
int g = v;
```
Now `g` is 42 as well. You can do math on the way, too:
```java
int v = 42;
int g = v + 10;
```
This time `g` is 52. Put this inside `main` and run it to see for yourself:
```java
int v = 42;
int g = v + 10;
System.out.println(g);
System.out.println("g");
```
```
52
g
```
It works like `5 + 3` in [Your First Program](ch01-01-first-program.md). Without quotes, Java works it out first. So `System.out.println(g);` prints the value stored in `g`, and `System.out.println("g");` prints the letter g.

A tip for trying out examples: two variables in the same `main` can't have the same name. If you paste in an example that makes a `v` when your `main` already has one, you'll get a red squiggly line. Java tells variables apart by their names, so two with the same name would be impossible to tell apart.

Decimals go in a `double`:
```java
double h = 3.9;
```
What happens if you put a decimal in an `int`?
```java
int v = 3.9;
```
That's an error. VS Code puts a red squiggly line under it, and the program won't run. An `int` only has room for whole numbers, so the `.9` has nowhere to go. Java won't throw part of your number away without telling you, so it stops and makes you fix it.

Here's the full list of Java's primitive types:

| `int`                 | `double`       | `float`                     | `char`                                                    | `byte`              | `boolean`                       | `long`            | `short`                               |
| --------------------- | -------------- | --------------------------- | --------------------------------------------------------- | ------------------- | ------------------------------- | ----------------- | ------------------------------------- |
| Standard whole number | Decimal number | Less precise decimal number | Single character, like `'a'` (stored as a number in code) | Really small number | `true` or `false`, nothing else | Really big number | Small number, but bigger than `byte`  |

Each one holds a single simple value: a number, a character, or true or false. That's what makes it a "primitive data type." These are the basic building blocks that come with Java.

{{#quiz ../quizzes/ch01-02-variables.toml}}

## Text
None of the primitive types can hold text, like a name. Text goes in a `String`:
```java
String name = "Sam";
System.out.println(name);
```
```
Sam
```
Strings always use *double* quotes (`"`), like the text we printed in [Your First Program](ch01-01-first-program.md). Single quotes (`'`) are only for a single `char`, like `'a'`.

`String` starts with a capital `S` because it isn't a primitive. It's a different kind of type, and the next lesson explains the difference.

You can join Strings together with `+`:
```java
String name = "Sam";
int score = 70;
System.out.println("Hi " + name);
System.out.println("Score: " + score);
```
```
Hi Sam
Score: 70
```
When one side of a `+` is text, Java turns the other side into text too and sticks them together. That makes it easy to label what you print.

It can also catch you out:
```java
System.out.println("Total: " + 2 + 3);
```
```
Total: 23
```
Java goes left to right. First `"Total: " + 2` makes the text `"Total: 2"`. Then `"Total: 2" + 3` sticks a `3` on the end. To do the math first, wrap it in parentheses like you would in math class:
```java
System.out.println("Total: " + (2 + 3));
```
```
Total: 5
```

{{#quiz ../quizzes/ch01-02-variables-text.toml}}

Every type so far comes built into Java. In the next lesson we'll make our own.
