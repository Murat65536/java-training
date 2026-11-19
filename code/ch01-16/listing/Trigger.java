public class Trigger {
	private final Condition condition;
	private final Runnable action;

	public Trigger(Condition triggerCondition, Runnable triggerAction) {
		condition = triggerCondition;
		action = triggerAction;
	}

	public void update() {
		if (condition.isMet()) {
			action.run();
		}
	}
}
