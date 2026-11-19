class Game {
	public static void main(String[] args) {
		Berserker zed = new Berserker(100);
		Player bob = new Player(100);

		zed.attack(bob);
		System.out.println("bob: " + bob.health); // bob: 99

		zed.takeDamage(85);
		zed.attack(bob);
		System.out.println("bob: " + bob.health); // bob: 97

		Player amy = new Player(100);
		amy.takeDamage(85);
		amy.attack(bob);
		System.out.println("bob: " + bob.health); // bob: 96
	}
}
