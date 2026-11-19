class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		Player alice = new Player(100);
		bob.takeDamage(50);

		bob.useAbility(alice);
		System.out.println("alice: " + alice.getHealth()); // alice: 100

		bob.learn(new Fireball());
		bob.useAbility(alice);
		System.out.println("alice: " + alice.getHealth()); // alice: 70

		bob.learn(new Drain());
		bob.useAbility(alice);
		System.out.println("alice: " + alice.getHealth()); // alice: 60
		System.out.println("bob: " + bob.getHealth()); // bob: 60

		Crate crate = new Crate();
		bob.useAbility(crate);
		System.out.println("crate: " + crate.getHealth()); // crate: 30

		bob.takeDamage(500);
		bob.useAbility(alice);
		System.out.println("alice: " + alice.getHealth()); // alice: 60
	}
}
