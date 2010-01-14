package arden.runtime;

public final class ArdenList extends ArdenValue {
	public final static ArdenList EMPTY = new ArdenList(new ArdenValue[0]);

	public ArdenValue[] values;

	public ArdenList(ArdenValue[] values) {
		this.values = values;
	}
	
	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		ArdenValue[] newValues = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			newValues[i] = values[i].setTime(newPrimaryTime);
		return new ArdenList(newValues);
	}
	
	@Override
	public ArdenValue[] getElements() {
		return values;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append('(');
		if (values.length == 1) {
			b.append(',');
			b.append(values[0].toString());
		} else if (values.length > 1) {
			b.append(values[0].toString());
			for (int i = 1; i < values.length; i++) {
				b.append(',');
				b.append(values[i].toString());
			}
		}
		b.append(')');
		return b.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ArdenList))
			return false;
		ArdenList list = (ArdenList) obj;
		if (list.values.length != values.length)
			return false;
		for (int i = 0; i < values.length; i++) {
			if (!values[i].equals(list.values[i]))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 1;
		for (ArdenValue val : values) {
			result *= 27;
			result += val.hashCode();
		}
		return result;
	}
}
