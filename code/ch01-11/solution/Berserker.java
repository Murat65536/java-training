class Berserker extends Player {
	Berserker(int startingHealth) {
		super(startingHealth);
	}

	@Override
	void attack(Player target) {
		super.attack(target);
		if (isInDanger()) {
			super.attack(target);
		}
	}
}
