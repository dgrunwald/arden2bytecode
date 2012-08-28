package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public class UntilEvokeEvent extends EvokeEvent {
	private EvokeEvent cycle;
	private ArdenTime until;
	
	public UntilEvokeEvent(EvokeEvent cycle, ArdenTime until) {
		this.cycle = cycle;
		this.until = until;
	}

	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		ArdenTime next = cycle.getNextRunTime(context);
		if (until.compareTo(next) > 0) {
			return next;
		}
		return null;
	}

	@Override
	public boolean runOnEvent(String event) {
		return cycle.runOnEvent(event);
	}
}
