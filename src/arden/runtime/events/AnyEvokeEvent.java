package arden.runtime.events;

import java.util.Arrays;
import java.util.List;

import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public class AnyEvokeEvent extends EvokeEvent {
	List<EvokeEvent> events;
	
	public AnyEvokeEvent(EvokeEvent[] events) {
		this.events = Arrays.asList(events);
	}

	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		ArdenTime min = null;
		for (EvokeEvent e : events) {
			ArdenTime nextRunTime = e.getNextRunTime(context); 
			if (nextRunTime.compareTo(min) < 0) {
				min = nextRunTime;
			}
		}
		return min;
	}

	@Override
	public boolean runOnEvent(String event) {
		boolean any = false;
		for (EvokeEvent e : events) {
			any = any || e.runOnEvent(event);
		}
		return any;
	}

}
