class Player {
	int health;
	int maxHealth;
	int x;

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
		x = x + steps;
		if (x < 0) {
			x = 0;
		} else if (x > 100) {
			x = 100;
		}
	}

	void respawn() {
		if (!isAlive()) {
			health = maxHealth;
			x = 0;
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
