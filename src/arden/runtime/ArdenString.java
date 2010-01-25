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
	public ArdenValue setTime(long newPrimaryTime) {
		return new ArdenString(value, newPrimaryTime);
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
		return b.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ArdenString) && value.equals(((ArdenString) obj).value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
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
