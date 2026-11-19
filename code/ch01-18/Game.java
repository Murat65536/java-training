class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		bob.equip(new Weapon("Sword", 25));

		Crate crate = new Crate();
		System.out.println("hits: " + bob.combo(crate)); // hits: 2
		System.out.println("crate: " + crate.getHealth()); // crate: 0

		Player alice = new Player(100);
		System.out.println("hits: " + bob.combo(alice)); // hits: 5
		System.out.println("alice: " + alice.getHealth()); // alice: 88

		Tank tom = new Tank(100);
		System.out.println("hits: " + bob.combo(tom)); // hits: 5
		System.out.println("tom: " + tom.getHealth()); // tom: 100
	}
}
