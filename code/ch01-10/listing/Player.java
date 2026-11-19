class Player {
	int health;
	int maxHealth;
	int x;
	Weapon weapon;

	Player(int startingHealth) {
		health = startingHealth;
		maxHealth = startingHealth;
	}

	void takeDamage(int amount) {
		health = health - amount;
		if (health < 0) {
			health = 0;
		}
	}

	void heal(int amount) {
		if (isAlive()) {
			health = health + amount;
			if (health > maxHealth) {
				health = maxHealth;
			}
		}
	}

	void move(int steps) {
		if (isAlive() && steps >= -10 && steps <= 10) {
			x = x + steps;
			if (x < 0) {
				x = 0;
			} else if (x > 100) {
				x = 100;
			}
		}
	}

	void respawn() {
		if (!isAlive()) {
			health = maxHealth;
			x = 0;
		}
	}

	void equip(Weapon newWeapon) {
		weapon = newWeapon;
	}

	void attack(Player target) {
		if (isAlive()) {
			if (weapon == null) {
				target.takeDamage(1);
			} else {
				target.takeDamage(weapon.damage);
			}
		}
	}

	void giveHealth(Player friend, int amount) {
		if (isAlive()) {
			takeDamage(amount);
			friend.heal(amount);
		}
	}

	boolean isAlive() {
		return health > 0;
	}

	boolean isInDanger() {
		return isAlive() && health < 20;
	}

	boolean isAtEdge() {
		return x == 0 || x == 100;
	}

	int missingHealth() {
		return maxHealth - health;
	}
}
