package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class FutureFixedDateEvokeEvent extends FixedDateEvokeEvent {

	private ArdenTime date;
	
	public FutureFixedDateEvokeEvent(ArdenTime date, long primaryTime) {
		super(date, primaryTime);
	}
	
	public FutureFixedDateEvokeEvent(ArdenTime date) {
		this(date, NOPRIMARYTIME);
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		if (context.getCurrentTime().compareTo(date) > 0) {
			return null;
		}
		return date;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new FutureFixedDateEvokeEvent(date, newPrimaryTime);
	}

}
