public class Fireball implements Ability {
	@Override
	public void use(Player user, Damageable target) {
		target.takeDamage(Constants.FIREBALL_DAMAGE);
	}
}
