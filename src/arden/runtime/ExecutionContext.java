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
	 * Calls another MLM.
	 * 
	 * @param name
	 *            The mlmname of the called MLM.
	 * @param institution
	 *            The institution of the called MLM. null if no institution was
	 *            specified.
	 * @param arguments
	 *            The arguments being passed. Can be null if no arguments were
	 *            specified.
	 * @param delay
	 *            The delay for calling the MLM (as ArdenDuration). When null is
	 *            passed, callMLM should wait until the executed MLM returns.
	 *            When a non-null duration is passed, callMLM should return null
	 *            immediately before starting execution of the called MLM.
	 * @return Returns the return value(s) of the called MLM. These are returned
	 *         only when calling without delay.
	 */
	public ArdenValue[] call(String name, String institution, ArdenValue[] arguments, ArdenValue delay) {
		throw new RuntimeException("CALL not implemented");
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
