public class Drain implements Ability {
	@Override
	public void use(Player user, Damageable target) {
		target.takeDamage(Constants.DRAIN_AMOUNT);
		user.heal(Constants.DRAIN_AMOUNT);
	}
}
