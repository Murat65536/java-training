class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		Player alice = new Player(100);
		Trigger rage = new Trigger(bob::isInDanger, () -> bob.attack(alice));

		rage.update();
		System.out.println("alice: " + alice.getHealth()); // alice: 100

		bob.takeDamage(85);
		rage.update();
		System.out.println("alice: " + alice.getHealth()); // alice: 99

		rage.update();
		rage.update();
		System.out.println("alice: " + alice.getHealth()); // alice: 99

		bob.heal(50);
		rage.update();
		bob.takeDamage(50);
		rage.update();
		System.out.println("alice: " + alice.getHealth()); // alice: 98
	}
}
