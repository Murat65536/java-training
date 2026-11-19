class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);

		bob.move(5);
		System.out.println("x: " + bob.x); // x: 5

		bob.move(50);
		System.out.println("x: " + bob.x); // x: 5

		bob.move(-12);
		System.out.println("x: " + bob.x); // x: 5

		bob.takeDamage(500);
		bob.move(5);
		System.out.println("x: " + bob.x); // x: 5
	}
}
