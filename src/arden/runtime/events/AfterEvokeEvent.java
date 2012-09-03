package arden.runtime.events;

import java.util.SortedSet;
import java.util.TreeSet;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ComparableArdenTime;
import arden.runtime.ExecutionContext;

public class AfterEvokeEvent extends EvokeEvent {

	EvokeEvent target;
	ArdenDuration duration;
	SortedSet<ComparableArdenTime> additionalSchedules;
	
	public AfterEvokeEvent(ArdenDuration duration, EvokeEvent target, long primaryTime) {
		super(primaryTime);
		this.duration = duration;
		this.target = target;
		this.additionalSchedules = new TreeSet<ComparableArdenTime>();
	}
	
	public AfterEvokeEvent(ArdenDuration duration, EvokeEvent target) {
		this(duration, target, NOPRIMARYTIME);
	}
	
	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		ArdenTime currentTime = context.getCurrentTime();
		ArdenTime nextRunTime = target.getNextRunTime(context);
		if (nextRunTime != null) {
			nextRunTime = new ArdenTime(nextRunTime.add(duration));
		}
		
		// delete past events from additionalSchedules:
		while (!additionalSchedules.isEmpty() && additionalSchedules.first().compareTo(currentTime) > 0) {
			additionalSchedules.remove(additionalSchedules.first());
		}
		
		// decide whether to use additionalSchedule time or nextRunTime:
		if (!additionalSchedules.isEmpty()) {
			if (additionalSchedules.first().toArdenTime().compareTo(nextRunTime) > 0) {
				return nextRunTime;
			} else {
				return additionalSchedules.first().toArdenTime();
			}
		}
		return nextRunTime;
	}

	@Override
	public boolean runOnEvent(String event, ExecutionContext context) {
		boolean run = target.runOnEvent(event, context);
		if (run) {
			// trigger in 'duration' after current time:
			additionalSchedules.add(new ComparableArdenTime(context.getCurrentTime().add(duration)));
		}
		return false;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new AfterEvokeEvent(duration, target, newPrimaryTime);
	}

}
