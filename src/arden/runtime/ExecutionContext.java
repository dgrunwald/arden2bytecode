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

	/**
	 * Called by write statements.
	 * 
	 * @param message
	 *            The message to be written.
	 * @param destination
	 *            The mapping clause describing the message destination.
	 */
	public void write(ArdenValue message, String destination) {
	}

	/**
	 * Retrieves another MLM.
	 * 
	 * @param name
	 *            The name of the requested MLM.
	 * @param institution
	 *            The institution of the requested MLM.
	 * @return The requested MLM.
	 */
	public MedicalLogicModule findModule(String name, String institution) {
		return null;
	}

	/**
	 * Calls another MLM using a delay.
	 * 
	 * @param mlm
	 *            The MLM that should be called.
	 * @param arguments
	 *            The arguments being passed. Can be null if no arguments were
	 *            specified.
	 * @param delay
	 *            The delay for calling the MLM (as ArdenDuration).
	 */
	public void callWithDelay(MedicalLogicModule mlm, ArdenValue[] arguments, ArdenValue delay) {
		throw new RuntimeException("callWithDelay not implemented");
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
