package arden.runtime;

import java.util.Date;

/**
 * Describes the environment in which a Medical Logic Module is executed.
 * 
 * @author Daniel Grunwald
 * 
 */
public class ExecutionContext {
	/**
	 * Creates a database query using a mapping clause. The DatabaseQuery object
	 * can be used to limit the number of results produced.
	 * 
	 * @param mapping
	 *            The contents of the mapping clause (=text between { and }).
	 *            The meaning is implementation-defined. The Arden language
	 *            specification uses mapping clauses like
	 *            "medication_cancellation where class = gentamicin".
	 * 
	 * @return This method may not return Java null. Instead, it can return
	 *         DatabaseQuery.NULL, a query that will always produce an empty
	 *         result set.
	 */
	public DatabaseQuery createQuery(String mapping) {
		return DatabaseQuery.NULL;
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
	public ArdenRunnable findModule(String name, String institution) {
		throw new RuntimeException("findModule not implemented");
	}

	/**
	 * Retrieves an interface implementation.
	 * 
	 * @param mapping
	 *            The mapping clause of the interface.
	 * @return The interface implementation.
	 */
	public ArdenRunnable findInterface(String mapping) {
		throw new RuntimeException("findInterface not implemented");
	}

	/**
	 * Calls another MLM using a delay.
	 * 
	 * @param mlm
	 *            The MLM that should be called. This will be an instance
	 *            returned from findModule() or findInterface().
	 * @param arguments
	 *            The arguments being passed. Can be null if no arguments were
	 *            specified.
	 * @param delay
	 *            The delay for calling the MLM (as ArdenDuration).
	 */
	public void callWithDelay(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay) {
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
