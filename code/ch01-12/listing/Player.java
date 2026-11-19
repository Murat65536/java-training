public class Player {
	private int health;
	private final int maxHealth;
	private int x;
	private Weapon weapon;

	public Player(int startingHealth) {
		health = startingHealth;
		maxHealth = startingHealth;
	}

	public int getHealth() {
		return health;
	}

	public int getX() {
		return x;
	}

	public void takeDamage(int amount) {
		health = health - amount;
		if (health < 0) {
			health = 0;
		}
	}

	public void heal(int amount) {
		if (isAlive()) {
			health = health + amount;
			if (health > maxHealth) {
				health = maxHealth;
			}
		}
	}

	public void move(int steps) {
		if (isAlive() && steps >= -10 && steps <= 10) {
			x = x + steps;
			if (x < 0) {
				x = 0;
			} else if (x > 100) {
				x = 100;
			}
		}
	}

	public void respawn() {
		if (!isAlive()) {
			health = maxHealth;
			x = 0;
		}
	}

	public void equip(Weapon newWeapon) {
		weapon = newWeapon;
	}

	public void attack(Player target) {
		if (isAlive()) {
			if (weapon == null) {
				target.takeDamage(1);
			} else {
				target.takeDamage(weapon.getDamage());
				weapon.use();
				if (weapon.isBroken()) {
					weapon = null;
				}
			}
		}
	}

	public void giveHealth(Player friend, int amount) {
		if (isAlive()) {
			takeDamage(amount);
			friend.heal(amount);
		}
	}

	public boolean isAlive() {
		return health > 0;
	}

	public boolean isInDanger() {
		return isAlive() && health < 20;
	}

	public boolean isAtEdge() {
		return x == 0 || x == 100;
	}

	public int missingHealth() {
		return maxHealth - health;
	}
}
