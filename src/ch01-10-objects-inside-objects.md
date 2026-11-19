# Objects Inside Objects

## A class for weapons
Players fight with bare hands right now, and *we* decide how much damage each attack does. Weapons would be better, with the weapon deciding the damage.
A weapon has a name and a damage amount, so it gets a class:
```java
class Weapon {
	String name;
	int damage;

	Weapon(String weaponName, int weaponDamage) {
		name = weaponName;
		damage = weaponDamage;
	}
}
```
Nothing here is new: two fields and a constructor with two parameters. It goes in a new file, `Weapon.java`, the same as `Player` has its own file.
```java
Weapon sword = new Weapon("Sword", 25);
System.out.println(sword.name + " does " + sword.damage + " damage");
```
```
Sword does 25 damage
```

## Giving players weapons
A field can be any type, including a class we wrote. So `Player` gets a field for its weapon:
```java
class Player {
	int health;
	int maxHealth;
	int x;
	Weapon weapon;

	// ...everything else stays the same
}
```
`Weapon weapon;` works like `int health;`. It says every `Player` has a `weapon`. Next, a method to hand a `Player` a weapon, and an `attack` where the weapon decides the damage:
```java
void equip(Weapon newWeapon) {
	weapon = newWeapon;
}

void attack(Player target) {
	if (isAlive()) {
		target.takeDamage(weapon.damage);
	}
}
```
`weapon.damage` means "look inside this `Player`'s `weapon` and get its `damage`." From outside the class, you can chain `.`s like this:
```java
Player bob = new Player(100);
Player alice = new Player(100);
bob.equip(new Weapon("Sword", 25));
System.out.println(bob.weapon.name);
bob.attack(alice);
System.out.println("alice: " + alice.health);
```
```
Sword
alice: 75
```

| `bob`        | `.weapon`                      | `.name`                    |
| ------------ | ------------------------------ | -------------------------- |
| A `Player` | That `Player`'s `Weapon` | That `Weapon`'s `name` |

Look at `bob.equip(new Weapon("Sword", 25));`. The `Weapon` is built right inside the `()`, without a variable to hold it first. That's fine, because `equip` stores its address in `bob.weapon`, so it isn't lost.

{{#quiz ../quizzes/ch01-10-weapons.toml}}

## Nothing at all
`bob` has a sword, but `alice` never got a weapon. What happens when `alice` attacks back?
```java
alice.attack(bob);
```
The program **crashes**, with something like this:
```
Exception in thread "main" java.lang.NullPointerException: Cannot read field "damage" because "this.weapon" is null
	at Player.attack(Player.java:52)
	at Game.main(Game.java:9)
```
As [Fields](ch01-04-fields.md) showed, fields have a value even if you never set them. Number fields start at `0`, and `boolean` fields start at `false`. Fields that hold objects start as **`null`**, which means "no object at all." In the address picture from the last lesson, `null` is a blank slip of paper. No address is written on it, so there's no house to look inside.

`alice.weapon` is `null`, so when `attack` asks for `weapon.damage`, there's no weapon to get the damage from. Java can't continue, so it stops the whole program.

### Reading the error
A crash message looks scary, but it's there to help. Line by line:
- `NullPointerException` names the problem: you tried to look inside something that was `null`.
- `Cannot read field "damage" because "this.weapon" is null` says what went wrong. `this` is Java's name for "the object the method was called on," which here is `alice`. So `this.weapon` means `alice`'s weapon.
- `at Player.attack(Player.java:52)` says where it happened: in `attack`, on line 52 of `Player.java`. VS Code shows line numbers down the left side of your code. (Your line number may be different, depending on how your file is laid out.)
- The next line, `at Game.main(Game.java:9)`, says where `attack` was called *from*: line 9 of `Game.java`, in `main`.

A crash isn't the same as a red squiggly line. Red squiggly lines mark mistakes VS Code can spot before the program starts. A crash happens while the program is running, so VS Code had no way to warn you.

`NullPointerException` will probably be the crash you see most, robot code included. Robot code is full of objects holding other objects, like an arm holding the motor that moves it. If one of them never gets set, this is the crash you get.

### Checking for null
Looking *inside* `null` is what crashes. Looking *at* it is fine:
```java
Player p = new Player(100);
System.out.println(p.weapon);
System.out.println(p.weapon == null);
```
```
null
true
```
So `==` can check for `null` before the code uses a weapon. Here, a `Player` without a weapon punches for 1 damage:
```java
void attack(Player target) {
	if (isAlive()) {
		if (weapon == null) {
			target.takeDamage(1);
		} else {
			target.takeDamage(weapon.damage);
		}
	}
}
```
Now nothing crashes:
```java
Player bob = new Player(100);
Player alice = new Player(100);
bob.equip(new Weapon("Sword", 25));
bob.attack(alice);
alice.attack(bob);
System.out.println("bob: " + bob.health);
System.out.println("alice: " + alice.health);
```
```
bob: 99
alice: 75
```

## Sharing a weapon
Everything from the last lesson about addresses still holds. If two players equip the *same* `Weapon`, they both have its address, so they share it:
```java
Weapon sword = new Weapon("Sword", 25);
Player bob = new Player(100);
Player alice = new Player(100);
bob.equip(sword);
alice.equip(sword);
sword.damage = 50;
System.out.println(bob.weapon.damage);
System.out.println(alice.weapon.damage);
```
```
50
50
```
There's one `new Weapon`, so there's one sword. Making it stronger makes it stronger for both of them.

{{#quiz ../quizzes/ch01-10-null.toml}}

## Putting it together
Here's the new `Weapon` class and the whole `Player` class. Only the files that changed are shown, and the eye button shows their unchanged parts.
{{#listing ch01-10}}

## Try it yourself
Weapons shouldn't last forever. Make them wear out:
1. Give `Weapon` a new `int` field named `durability`, and have the constructor set it to `3`.
2. Every time a `Player` attacks with a weapon, lower that weapon's `durability` by 1.
3. When a weapon's `durability` reaches 0, it breaks. The `Player` no longer has a weapon and goes back to punching.

*Hint: how do you write "doesn't have a weapon"?*

To check your answer, make your changes to `Weapon.java` and `Player.java`, and replace the `main` in `Game.java` with this one:
```java
{{#include ../code/ch01-10/Game.java}}
```
If every line prints what its comment says, your weapons wear out correctly.

<details>
<summary>Answer</summary>

Here's one way to do it:
{{#solution ch01-10}}
`Weapon` gets a `durability` field that its constructor sets to `3`. `attack` lowers it after every hit and throws the weapon away when it breaks.
Setting `weapon` to `null` is how you say "this `Player` doesn't have a weapon anymore." From then on, the `weapon == null` check makes them punch.

</details>
