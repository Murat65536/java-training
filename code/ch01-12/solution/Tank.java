public class Tank extends Player {
	public Tank(int startingHealth) {
		super(startingHealth);
	}

	@Override
	public void takeDamage(int amount) {
		if (amount > 5) {
			super.takeDamage(amount - 5);
		}
	}
}
