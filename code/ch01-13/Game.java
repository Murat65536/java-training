class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);

		bob.takeDamage(30);
		System.out.println("health: " + bob.getHealth()); // health: 70

		bob.heal(1000);
		System.out.println("health: " + bob.getHealth()); // health: 100

		bob.move(8);
		bob.move(-3);
		System.out.println("x: " + bob.getX()); // x: 5

		bob.move(-10);
		System.out.println("x: " + bob.getX()); // x: 0

		bob.takeDamage(500);
		System.out.println("health: " + bob.getHealth()); // health: 0
	}
}
