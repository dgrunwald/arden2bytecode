package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class FixedDateEvokeEvent extends EvokeEvent {

	private ArdenTime date;
	
	public FixedDateEvokeEvent(ArdenTime date, long primaryTime) {
		super(primaryTime);
		this.date = date;
	}
	
	public FixedDateEvokeEvent(ArdenTime date) {
		this(date, NOPRIMARYTIME);
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		return date;
	}

	@Override
	public boolean runOnEvent(String event, ExecutionContext context) {
		return false;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new FixedDateEvokeEvent(date, newPrimaryTime);
	}

}
