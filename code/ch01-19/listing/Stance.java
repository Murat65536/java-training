public enum Stance {
	NORMAL(1.0, 0),
	DEFENDING(0.5, 0),
	AGGRESSIVE(1.5, 5);

	private final double damageMultiplier;
	private final int attackBonus;

	Stance(double stanceDamageMultiplier, int stanceAttackBonus) {
		damageMultiplier = stanceDamageMultiplier;
		attackBonus = stanceAttackBonus;
	}

	public double getDamageMultiplier() {
		return damageMultiplier;
	}

	public int getAttackBonus() {
		return attackBonus;
	}
}
