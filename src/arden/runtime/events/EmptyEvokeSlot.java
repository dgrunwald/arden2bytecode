package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class EmptyEvokeSlot extends EvokeEvent {

	public EmptyEvokeSlot(long primaryTime) {
		super(primaryTime);
	}
	
	public EmptyEvokeSlot() {
		this(NOPRIMARYTIME);
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		return null;
	}

	@Override
	public boolean runOnEvent(String event, ExecutionContext context) {
		return false;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new EmptyEvokeSlot(newPrimaryTime);
	}

}
