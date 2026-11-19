class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		Player alice = new Player(100);
		bob.equip(new Weapon("Sword", 25));

		bob.attack(alice);
		bob.attack(alice);
		bob.attack(alice);
		System.out.println("alice: " + alice.health); // alice: 25
		System.out.println(bob.weapon == null); // true

		bob.attack(alice);
		System.out.println("alice: " + alice.health); // alice: 24
	}
}
