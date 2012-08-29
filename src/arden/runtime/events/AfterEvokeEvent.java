package arden.runtime.events;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class AfterEvokeEvent extends EvokeEvent {

	EvokeEvent target;
	ArdenDuration duration;
	
	public AfterEvokeEvent(ArdenDuration duration, EvokeEvent target, long primaryTime) {
		super(primaryTime);
		this.duration = duration;
		this.target = target;
	}
	
	public AfterEvokeEvent(ArdenDuration duration, EvokeEvent target) {
		this(duration, target, NOPRIMARYTIME);
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		return new ArdenTime(target.getNextRunTime(context).add(duration));
	}

	@Override
	public boolean runOnEvent(String event) {
		return target.runOnEvent(event);
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new AfterEvokeEvent(duration, target, newPrimaryTime);
	}

}
