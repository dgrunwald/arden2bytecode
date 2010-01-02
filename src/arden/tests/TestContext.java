package arden.tests;

import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class TestContext extends ExecutionContext {

	@Override
	public ArdenValue read(String query) {
		throw new RuntimeException("Unexpected read");
	}

	StringBuilder b = new StringBuilder();

	@Override
	public void write(ArdenValue message) {
		b.append(((ArdenString)message).value);
		b.append("\n");
	}

	public String getOutputText() {
		return b.toString();
	}
}
