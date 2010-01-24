package arden.tests;

import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

public class TestContext extends ExecutionContext {
	StringBuilder b = new StringBuilder();

	@Override
	public void write(ArdenValue message, String destination) {
		b.append(((ArdenString)message).value);
		b.append("\n");
	}

	public String getOutputText() {
		return b.toString();
	}
}
