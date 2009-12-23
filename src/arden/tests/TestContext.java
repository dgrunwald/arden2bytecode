package arden.tests;

import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class TestContext implements ExecutionContext {

	@Override
	public ArdenValue read(String query) {
		throw new RuntimeException("Unexpected read");
	}

	StringBuilder b = new StringBuilder();
	
	@Override
	public void write(String message) {
		b.append(message);
		b.append("\n");
	}
	
	public String getOutputText() {
		return b.toString();
	}
}
