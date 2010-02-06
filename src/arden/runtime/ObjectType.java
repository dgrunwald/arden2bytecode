package arden.runtime;

/**
 * Represents the runtime type of an ArdenObject.
 * 
 * @author Daniel Grunwald
 * 
 */
public class ObjectType {
	public final String name;
	public final String[] fieldNames;

	public ObjectType(String name, String[] fieldNames) {
		if (name == null || fieldNames == null)
			throw new NullPointerException();
		this.name = name;
		this.fieldNames = fieldNames;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(name);
		b.append(" := OBJECT [ ");
		for (int i = 0; i < fieldNames.length; i++) {
			if (i > 0)
				b.append(", ");
			b.append(fieldNames[i]);
		}
		b.append(" ]");
		return b.toString();
	}
}
