package arden.runtime;

import java.util.HashMap;

public final class ArdenObject extends ArdenValue {
	private HashMap<String, ArdenValue> attributes = new HashMap<String, ArdenValue>();
	
	public ArdenValue getAttribute(String name) {
		ArdenValue result = attributes.get(name.toLowerCase());
		if (result == null)
			return ArdenNull.INSTANCE;
		else
			return result;
	}
	
	public void setAttribute(String name, ArdenValue value) {
		attributes.put(name.toLowerCase(), value);
	}
}
