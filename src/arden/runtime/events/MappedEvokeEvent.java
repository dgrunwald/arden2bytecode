package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public class MappedEvokeEvent extends EvokeEvent {
	private String mapping;
	
	public MappedEvokeEvent(String mapping) {
		if (mapping == null) {
			throw new NullPointerException();
		}
		this.mapping = mapping;
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		return null;
	}

	@Override
	public boolean runOnEvent(String event) {
		if (mapping.equalsIgnoreCase(event)) {
			return true;
		}
		return false;
	}

}
