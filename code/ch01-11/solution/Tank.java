class Tank extends Player {
	Tank(int startingHealth) {
		super(startingHealth);
	}

	@Override
	void takeDamage(int amount) {
		if (amount > 5) {
			super.takeDamage(amount - 5);
		}
	}
}
