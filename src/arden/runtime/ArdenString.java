package arden.runtime;

public final class ArdenString extends ArdenValue {
	public final String value;

	public ArdenString(String value) {
		this.value = value;
	}

	public ArdenString(String value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			b.append(c);
			if (c == '"')
				b.append('"'); // double "
		}
		b.append('"');
		b.append(primaryTimeToString());
		return b.toString();
	}

	@Override
	public int compareTo(ArdenValue rhs) {
		if (rhs instanceof ArdenString) {
			return Integer.signum(value.compareTo(((ArdenString) rhs).value));
		} else {
			return Integer.MIN_VALUE;
		}
	}
}
