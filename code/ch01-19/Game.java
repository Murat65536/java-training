import java.util.ArrayList;

class Game {
	public static void main(String[] args) {
		Player bob = new Player(100);
		Player alice = new Player(100);
		Player carl = new Player(100);
		Crate crate = new Crate();
		alice.takeDamage(30);
		carl.takeDamage(70);

		ArrayList<Damageable> targets = new ArrayList<>();
		targets.add(alice);
		targets.add(carl);
		targets.add(crate);

		bob.attackWeakest(targets);
		System.out.println("carl: " + carl.getHealth()); // carl: 29

		carl.takeDamage(500);
		bob.attackWeakest(targets);
		System.out.println("crate: " + crate.getHealth()); // crate: 39
		System.out.println("alice: " + alice.getHealth()); // alice: 70

		ArrayList<Damageable> nobody = new ArrayList<>();
		bob.attackWeakest(nobody);
		System.out.println("score: " + bob.getScore()); // score: 0
	}
}
