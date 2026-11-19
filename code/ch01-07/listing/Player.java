class Player {
	int health;
	int maxHealth;

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
		}
	}

	boolean isAlive() {
		return health > 0;
	}

	int missingHealth() {
		return maxHealth - health;
	}
}
