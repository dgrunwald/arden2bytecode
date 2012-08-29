package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class UndefinedEvokeEvent extends EvokeEvent {

	public UndefinedEvokeEvent(long primaryTime) {
		super(primaryTime);
	}
	
	public UndefinedEvokeEvent() {
		this(NOPRIMARYTIME);
	}
	
	@Override
	public boolean runOnEvent(String event) {
		return false;
	}

	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		return null;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		// TODO Auto-generated method stub
		return null;
	}
}
