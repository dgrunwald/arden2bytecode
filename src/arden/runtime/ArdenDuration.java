package arden.runtime;

public final class ArdenDuration extends ArdenValue {
	public static final double SECONDS_PER_MONTH = 2629746;

	public final boolean isMonths;
	public final double value;

	public ArdenDuration(double value, boolean isMonths, long primaryTime) {
		super(primaryTime);
		this.value = value;
		this.isMonths = isMonths;
	}

	public static ArdenDuration seconds(double seconds, long primaryTime) {
		return new ArdenDuration(seconds, false, primaryTime);
	}

	public static ArdenDuration months(double months, long primaryTime) {
		return new ArdenDuration(months, true, primaryTime);
	}

	@Override
	public String toString() {
		return value + " " + (isMonths ? "months" : "seconds") + primaryTimeToString();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO: implement this
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public int hashCode() {
		// TODO: implement this
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public int compareTo(ArdenValue rhs) {
		// TODO: implement this
		throw new RuntimeException("NOT IMPLEMENTED");
	}
}
