public class Crate implements Damageable {
	private int health;

	public Crate() {
		health = Constants.CRATE_HEALTH;
	}

	@Override
	public void takeDamage(int amount) {
		health = Math.max(health - amount, 0);
	}

	@Override
	public int getHealth() {
		return health;
	}
}
