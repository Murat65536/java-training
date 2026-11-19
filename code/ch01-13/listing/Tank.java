public class Tank extends Player {
	public Tank(int startingHealth) {
		super(startingHealth);
	}

	@Override
	public void takeDamage(int amount) {
		if (amount > Constants.TANK_ARMOR) {
			super.takeDamage(amount - Constants.TANK_ARMOR);
		}
	}
}
