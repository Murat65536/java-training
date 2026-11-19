public class Healer extends Player {
	public Healer(int startingHealth) {
		super(startingHealth);
	}

	public void healFriend(Player friend) {
		if (isAlive()) {
			friend.heal(Constants.HEALER_HEAL_AMOUNT);
		}
	}
}
