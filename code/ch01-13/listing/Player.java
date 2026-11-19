public class Player {
	private static int playersCreated;

	private int health;
	private final int maxHealth;
	private int x;
	private Weapon weapon;
	private int score;

	public Player(int startingHealth) {
		health = startingHealth;
		maxHealth = startingHealth;
		playersCreated = playersCreated + 1;
	}

	public static int getPlayersCreated() {
		return playersCreated;
	}

	public int getHealth() {
		return health;
	}

	public int getX() {
		return x;
	}

	public int getScore() {
		return score;
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
		if (isAlive() && steps >= -Constants.MAX_STEPS && steps <= Constants.MAX_STEPS) {
			x = x + steps;
			if (x < 0) {
				x = 0;
			} else if (x > Constants.ARENA_SIZE) {
				x = Constants.ARENA_SIZE;
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
		boolean wasAlive = target.isAlive();
		if (isAlive()) {
			if (weapon == null) {
				target.takeDamage(Constants.PUNCH_DAMAGE);
			} else {
				target.takeDamage(weapon.getDamage());
				weapon.use();
				if (weapon.isBroken()) {
					weapon = null;
				}
			}
		}
		if (wasAlive && !target.isAlive()) {
			score = score + 1;
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
		return isAlive() && health < Constants.DANGER_HEALTH;
	}

	public boolean isAtEdge() {
		return x == 0 || x == Constants.ARENA_SIZE;
	}

	public int missingHealth() {
		return maxHealth - health;
	}
}
