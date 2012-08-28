package arden.runtime.events;

import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;


public abstract class EvokeEvent {
	/** next run time for a scheduled event */
	public abstract ArdenTime getNextRunTime(ExecutionContext context);
	
	/** an event such as 'penicillin_storage' occurred */
	public abstract boolean runOnEvent(String event);
}
