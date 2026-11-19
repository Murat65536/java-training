public class Abilities {
	public static final Ability FIREBALL = (user, target) -> target.takeDamage(Constants.FIREBALL_DAMAGE);

	public static final Ability DRAIN = (user, target) -> {
		target.takeDamage(Constants.DRAIN_AMOUNT);
		user.heal(Constants.DRAIN_AMOUNT);
	};
}
