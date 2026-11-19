class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		Player alice = new Player(100);
		bob.equip(new Weapon("Sword", 25));

		bob.attack(alice);
		System.out.println("alice: " + alice.getHealth()); // alice: 75

		bob.attack(alice);
		System.out.println("alice: " + alice.getHealth()); // alice: 59

		bob.attack(alice);
		System.out.println("alice: " + alice.getHealth()); // alice: 51

		bob.attack(alice);
		System.out.println("alice: " + alice.getHealth()); // alice: 50

		System.out.println(alice.getHealthPercent()); // 50.0
	}
}
