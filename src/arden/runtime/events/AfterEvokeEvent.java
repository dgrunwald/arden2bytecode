package arden.runtime.events;

import java.util.SortedSet;
import java.util.TreeSet;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class AfterEvokeEvent extends EvokeEvent {

	EvokeEvent target;
	ArdenDuration duration;
	SortedSet<ArdenTime> additionalSchedules;
	
	public AfterEvokeEvent(ArdenDuration duration, EvokeEvent target, long primaryTime) {
		super(primaryTime);
		this.duration = duration;
		this.target = target;
		this.additionalSchedules = new TreeSet<ArdenTime>(new ArdenTime.NaturalComparator());
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
		while (!additionalSchedules.isEmpty() && additionalSchedules.comparator().compare(currentTime, additionalSchedules.first()) > 0) {
			additionalSchedules.remove(additionalSchedules.first());
		}
		
		// decide whether to use additionalSchedule time or nextRunTime:
		if (!additionalSchedules.isEmpty()) {
			if (additionalSchedules.comparator().compare(additionalSchedules.first(), nextRunTime) > 0) {
				return nextRunTime;
			} else {
				return additionalSchedules.first();
			}
		}
		return nextRunTime;
	}

	@Override
	public boolean runOnEvent(String event, ExecutionContext context) {
		boolean run = target.runOnEvent(event, context);
		if (run) {
			// trigger in 'duration' after current time:
			additionalSchedules.add(new ArdenTime(context.getCurrentTime().add(duration)));
		}
		return false;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new AfterEvokeEvent(duration, target, newPrimaryTime);
	}

}
