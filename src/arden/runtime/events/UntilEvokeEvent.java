package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class UntilEvokeEvent extends EvokeEvent {
	private EvokeEvent cycle;
	private ArdenTime until;
	
	public UntilEvokeEvent(EvokeEvent cycle, ArdenTime until, long primaryTime) {
		super(primaryTime);
		this.cycle = cycle;
		this.until = until;
	}
	
	public UntilEvokeEvent(EvokeEvent cycle, ArdenTime until) {
		this(cycle, until, NOPRIMARYTIME);
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
	public boolean runOnEvent(String event, ExecutionContext context) {
		return cycle.runOnEvent(event, context);
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new UntilEvokeEvent(cycle, until, newPrimaryTime);
	}
}
