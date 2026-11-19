class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		Player alice = new Player(100);
		alice.takeDamage(50);

		bob.giveHealth(alice, 30);
		System.out.println("bob: " + bob.health); // bob: 70
		System.out.println("alice: " + alice.health); // alice: 80

		bob.takeDamage(500);
		bob.giveHealth(alice, 10);
		System.out.println("alice: " + alice.health); // alice: 80
	}
}
