package arden.runtime.events;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class CyclicEvokeEvent extends EvokeEvent {
	
	private ArdenDuration interval;
	private ArdenDuration length;
	private ArdenTime starting;
	private ArdenTime next;
	
	public CyclicEvokeEvent(ArdenDuration interval, ArdenDuration length, ArdenTime starting, long primaryTime) {
		this.interval = interval;
		this.length = length;
		this.starting = starting;
		this.next = this.starting;
	}
	
	public CyclicEvokeEvent(ArdenDuration interval, ArdenDuration length, ArdenTime starting) {
		this(interval, length, starting, NOPRIMARYTIME);
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		ArdenTime current = context.getCurrentTime();
		ArdenTime limit = new ArdenTime(starting.add(length)); 
		while (current.compareTo(next) > 0) {
			next = new ArdenTime(next.add(interval));
			if (next.compareTo(limit) > 0) {
				return null;
			}
		}
		return next;
	}

	@Override
	public boolean runOnEvent(String event, ExecutionContext context) {
		return false;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new CyclicEvokeEvent(interval, length, starting, newPrimaryTime);
	}

}
