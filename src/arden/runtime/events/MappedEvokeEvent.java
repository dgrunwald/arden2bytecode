package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class MappedEvokeEvent extends EvokeEvent {
	private String mapping;
	
	public MappedEvokeEvent(String mapping, long primaryTime) {
		super(primaryTime);
		if (mapping == null) {
			throw new NullPointerException();
		}
		this.mapping = mapping;
	}
	
	public MappedEvokeEvent(String mapping) {
		this(mapping, NOPRIMARYTIME);
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		return null;
	}

	@Override
	public boolean runOnEvent(String event, ExecutionContext context) {
		if (mapping.equalsIgnoreCase(event)) {
			return true;
		}
		return false;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new MappedEvokeEvent(mapping, newPrimaryTime);
	}

}
