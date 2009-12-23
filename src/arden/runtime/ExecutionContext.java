package arden.runtime;

/**
 * Describes the environment in which a Medical Logic Module is executed.
 * 
 * @author Daniel Grunwald
 * 
 */
public interface ExecutionContext {
	// query = "medication_cancellation where class = gentamicin"
	ArdenValue read(String query);

	// called by write statements
	void write(String message);
}
