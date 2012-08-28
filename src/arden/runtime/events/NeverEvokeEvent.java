package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public class NeverEvokeEvent extends EvokeEvent {

	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		return null;
	}

	@Override
	public boolean runOnEvent(String event) {
		return false;
	}
}
