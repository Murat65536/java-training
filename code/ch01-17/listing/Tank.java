public class Tank extends Player {
	public Tank(int startingHealth) {
		super(startingHealth);
	}

	@Override
	public void takeDamage(int amount) {
		super.takeDamage((int) (amount * Constants.TANK_DAMAGE_MULTIPLIER));
	}
}
