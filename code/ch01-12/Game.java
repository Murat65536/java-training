class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		Player alice = new Player(30);
		bob.equip(new Weapon("Sword", 25));

		bob.attack(alice);
		System.out.println("score: " + bob.getScore()); // score: 0

		bob.attack(alice);
		System.out.println("score: " + bob.getScore()); // score: 1

		bob.attack(alice);
		System.out.println("score: " + bob.getScore()); // score: 1

		// bob.score = 100;
	}
}
