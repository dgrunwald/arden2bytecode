package arden.runtime;

public class ArdenObject extends ArdenValue {
	public final ObjectType type;
	public final ArdenValue[] fields;

	public ArdenObject(ObjectType type) {
		this.type = type;
		this.fields = new ArdenValue[type.fieldNames.length];
		for (int i = 0; i < fields.length; i++)
			fields[i] = ArdenNull.INSTANCE;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		for (int i = 0; i < fields.length; i++)
			fields[i] = fields[i].setTime(newPrimaryTime);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("NEW ");
		b.append(type.name);
		b.append(" WITH ");
		for (int i = 0; i < fields.length; i++) {
			if (i > 0)
				b.append(", ");
			b.append(fields[i].toString());
		}
		return b.toString();
	}
}
