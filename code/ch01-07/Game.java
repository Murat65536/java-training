class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		bob.takeDamage(30);

		bob.heal(10);
		System.out.println(bob.health); // 80

		bob.heal(1000);
		System.out.println(bob.health); // 100
	}
}
