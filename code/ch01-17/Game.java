class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		Player alice = new Player(100);

		bob.setStance(Stance.DEFENDING);
		bob.takeDamage(20);
		bob.attack(alice);
		System.out.println("bob: " + bob.getHealth()); // bob: 90
		System.out.println("alice: " + alice.getHealth()); // alice: 100

		bob.setStance(Stance.AGGRESSIVE);
		bob.takeDamage(20);
		bob.attack(alice);
		System.out.println("bob: " + bob.getHealth()); // bob: 60
		System.out.println("alice: " + alice.getHealth()); // alice: 94

		bob.setStance(Stance.NORMAL);
		bob.takeDamage(20);
		bob.attack(alice);
		System.out.println("bob: " + bob.getHealth()); // bob: 40
		System.out.println("alice: " + alice.getHealth()); // alice: 93
		System.out.println(bob.getStance()); // NORMAL
	}
}
