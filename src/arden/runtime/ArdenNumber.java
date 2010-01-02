package arden.runtime;

public final class ArdenNumber extends ArdenValue {
	public final double value;

	public ArdenNumber(double value) {
		this.value = value;
	}

	public ArdenNumber(double value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	@Override
	public String toString() {
		return Double.toString(value) + primaryTimeToString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ArdenNumber) && (value == ((ArdenNumber) obj).value);
	}

	@Override
	public int hashCode() {
		return new Double(value).hashCode();
	}
}
