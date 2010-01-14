package arden.runtime;

public final class ArdenNumber extends ArdenValue {
	public static final ArdenNumber ZERO = new ArdenNumber(0);

	public final double value;

	public ArdenNumber(double value) {
		this.value = value;
	}

	private ArdenNumber(double value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	public static ArdenValue create(double value, long primaryTime) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return ArdenNull.create(primaryTime);
		else
			return new ArdenNumber(value, primaryTime);
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return create(value, newPrimaryTime);
	}

	@Override
	public String toString() {
		return toString(value);
	}

	public static String toString(double num) {
		int i = (int) num;
		if (i == num)
			return Integer.toString(i);
		else
			return Double.toString(num);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ArdenNumber) && (value == ((ArdenNumber) obj).value);
	}

	@Override
	public int hashCode() {
		return new Double(value).hashCode();
	}

	@Override
	public int compareTo(ArdenValue rhs) {
		if (rhs instanceof ArdenNumber) {
			double rval = ((ArdenNumber) rhs).value;
			if (value < rval)
				return -1;
			else if (value > rval)
				return 1;
			else
				return 0;
		}
		return Integer.MIN_VALUE;
	}
}
