public class Weapon {
	private final String name;
	private final int damage;
	private int durability;

	public Weapon(String weaponName, int weaponDamage) {
		name = weaponName;
		damage = weaponDamage;
		durability = 3;
	}

	public int getDamage() {
		return damage;
	}

	public void use() {
		if (durability > 0) {
			durability = durability - 1;
		}
	}

	public boolean isBroken() {
		return durability == 0;
	}
}
