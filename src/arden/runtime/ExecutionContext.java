package arden.runtime;

import java.util.Date;

/**
 * Describes the environment in which a Medical Logic Module is executed.
 * 
 * @author Daniel Grunwald
 * 
 */
public class ExecutionContext {
	// query = "medication_cancellation where class = gentamicin"
	public ArdenValue read(String query) {
		return ArdenNull.INSTANCE;
	}

	/** Called by write statements */
	public void write(ArdenValue message) {
	}

	private ArdenTime eventtime = new ArdenTime(new Date());

	/** Gets the eventtime. */
	public ArdenTime getEventTime() {
		return eventtime;
	}

	/** Gets the triggertime. */
	public ArdenTime getTriggerTime() {
		return eventtime;
	}

	/** Gets the current time. */
	public ArdenTime getCurrentTime() {
		return new ArdenTime(new Date());
	}
}
