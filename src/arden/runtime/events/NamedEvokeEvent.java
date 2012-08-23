package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public class NamedEvokeEvent extends EvokeEvent {
	private String name;
	
	public NamedEvokeEvent(String name) {
		if (name == null) {
			throw new RuntimeException("no name given to event");
		}
		this.name = name;
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		return null;
	}

	@Override
	public boolean runOnEvent(String event) {
		if (name.equalsIgnoreCase(event)) {
			return true;
		}
		return false;
	}

}
