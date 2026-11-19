class Healer extends Player {
	Healer(int startingHealth) {
		super(startingHealth);
	}

	void healFriend(Player friend) {
		if (isAlive()) {
			friend.heal(20);
		}
	}
}
