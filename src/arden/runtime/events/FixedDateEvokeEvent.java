package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public class FixedDateEvokeEvent extends EvokeEvent {

	private ArdenTime date;
	
	public FixedDateEvokeEvent(ArdenTime date) {
		this.date = date;
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		if (context.getCurrentTime().compareTo(date) > 0) {
			return null;
		}
		return date;
	}

	@Override
	public boolean runOnEvent(String event) {
		return false;
	}

}
