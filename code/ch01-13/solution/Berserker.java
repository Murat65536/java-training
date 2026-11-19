public class Berserker extends Player {
	public Berserker(int startingHealth) {
		super(startingHealth);
	}

	@Override
	public void attack(Player target) {
		super.attack(target);
		if (isInDanger()) {
			super.attack(target);
		}
	}
}
