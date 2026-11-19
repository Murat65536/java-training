public class Trigger {
	private final Condition condition;
	private final Runnable action;
	private boolean wasMet;

	public Trigger(Condition triggerCondition, Runnable triggerAction) {
		condition = triggerCondition;
		action = triggerAction;
	}

	public void update() {
		boolean isMet = condition.isMet();
		if (isMet && !wasMet) {
			action.run();
		}
		wasMet = isMet;
	}
}
