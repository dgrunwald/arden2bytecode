package arden.runtime;

public interface ExecutionContext {
	// query = "medication_cancellation where class = gentamicin"
	ArdenValue read(String query);
	
	// called by write statements
	void write(String message);
}
