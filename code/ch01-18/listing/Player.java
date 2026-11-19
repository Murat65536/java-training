public class Player implements Damageable {
	private static int playersCreated;

	private int health;
	private final int maxHealth;
	private int x;
	private Weapon weapon;
	private int score;
	private Ability ability;
	private Stance stance = Stance.NORMAL;

	public Player(int startingHealth) {
		health = startingHealth;
		maxHealth = startingHealth;
		playersCreated = playersCreated + 1;
	}

	public static int getPlayersCreated() {
		return playersCreated;
	}

	@Override
	public int getHealth() {
		return health;
	}

	public int getX() {
		return x;
	}

	public int getScore() {
		return score;
	}

	public Stance getStance() {
		return stance;
	}

	public void setStance(Stance newStance) {
		stance = newStance;
	}

	public double getHealthPercent() {
		return (double) health / maxHealth * 100;
	}

	@Override
	public void takeDamage(int amount) {
		health = Math.max(health - (int) (amount * stance.getDamageMultiplier()), 0);
	}

	public void heal(int amount) {
		if (isAlive()) {
			health = Math.min(health + amount, maxHealth);
		}
	}

	public void move(int steps) {
		if (isAlive() && steps >= -Constants.MAX_STEPS && steps <= Constants.MAX_STEPS) {
			x = Math.max(0, Math.min(x + steps, Constants.ARENA_SIZE));
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

	public void attack(Damageable target) {
		boolean wasAlive = target.getHealth() > 0;
		if (isAlive() && stance != Stance.DEFENDING) {
			if (weapon == null) {
				target.takeDamage(Constants.PUNCH_DAMAGE + stance.getAttackBonus());
			} else {
				target.takeDamage(weapon.getDamage() + stance.getAttackBonus());
				weapon.use();
				if (weapon.isBroken()) {
					weapon = null;
				}
			}
		}
		if (wasAlive && target.getHealth() == 0) {
			score = score + 1;
		}
	}

	public void attackTimes(Damageable target, int times) {
		for (int i = 0; i < times; i++) {
			attack(target);
		}
	}

	public void learn(Ability newAbility) {
		ability = newAbility;
	}

	public void useAbility(Damageable target) {
		if (isAlive() && ability != null) {
			ability.use(this, target);
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
