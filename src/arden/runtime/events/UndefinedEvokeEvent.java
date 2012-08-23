package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public class UndefinedEvokeEvent extends EvokeEvent {

	@Override
	public boolean runOnEvent(String event) {
		return false;
	}

	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		return null;
	}
}
