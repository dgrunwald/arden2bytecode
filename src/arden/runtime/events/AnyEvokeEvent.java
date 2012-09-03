package arden.runtime.events;

import java.util.Arrays;
import java.util.List;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class AnyEvokeEvent extends EvokeEvent {
	List<EvokeEvent> events;
	
	public AnyEvokeEvent(EvokeEvent[] events, long primaryTime) {
		this.events = Arrays.asList(events);
	}
	
	public AnyEvokeEvent(EvokeEvent[] events) {
		this(events, NOPRIMARYTIME);
	}
	
	public AnyEvokeEvent(List<EvokeEvent> events, long primaryTime) {
		super(primaryTime);
		this.events = events;
	}

	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		ArdenTime min = null;
		for (EvokeEvent e : events) {
			ArdenTime nextRunTime = e.getNextRunTime(context); 
			if (min == null || min.compareTo(nextRunTime) > 0) {
				min = nextRunTime;
			}
		}
		if (context.getCurrentTime().compareTo(min) > 0) {
			return null; // event is in the past
		}
		return min;
	}

	@Override
	public boolean runOnEvent(String event, ExecutionContext context) {
		boolean any = false;
		for (EvokeEvent e : events) {
			any = any || e.runOnEvent(event, context);
		}
		return any;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new AnyEvokeEvent(events, newPrimaryTime);
	}

}
