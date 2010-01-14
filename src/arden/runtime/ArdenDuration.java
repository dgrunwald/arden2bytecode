package arden.runtime;

public final class ArdenDuration extends ArdenValue {
	public static final double SECONDS_PER_MONTH = 2629746;

	public final boolean isMonths;
	public final double value;

	private ArdenDuration(double value, boolean isMonths, long primaryTime) {
		super(primaryTime);
		this.value = value;
		this.isMonths = isMonths;
	}

	public static ArdenValue create(double value, boolean isMonths, long primaryTime) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return ArdenNull.create(primaryTime);
		else
			return new ArdenDuration(value, isMonths, primaryTime);
	}

	public static ArdenValue seconds(double seconds, long primaryTime) {
		return create(seconds, false, primaryTime);
	}

	public static ArdenValue months(double months, long primaryTime) {
		return create(months, true, primaryTime);
	}

	public double toSeconds() {
		if (isMonths)
			return value * SECONDS_PER_MONTH;
		else
			return value;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return create(value, isMonths, newPrimaryTime);
	}

	@Override
	public String toString() {
		String unit;
		double val = value;
		if (isMonths) {
			unit = "months";
			if (val % 12 == 0) {
				val /= 12;
				unit = "years";
			}
		} else {
			unit = "seconds";
			if (val % 60 == 0) {
				val /= 60;
				unit = "minutes";
				if (val % 60 == 0) {
					val /= 60;
					unit = "hours";
					if (val % 24 == 0) {
						val /= 24;
						unit = "days";
					}
				}
			}
		}
		if (val == 1)
			return "1 " + unit.substring(0, unit.length() - 1);
		else
			return ArdenNumber.toString(val) + " " + unit;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ArdenDuration && ((ArdenDuration) obj).toSeconds() == toSeconds();
	}

	@Override
	public int hashCode() {
		return new Double(toSeconds()).hashCode();
	}

	@Override
	public int compareTo(ArdenValue rhs) {
		if (rhs instanceof ArdenDuration) {
			double rval = ((ArdenDuration) rhs).toSeconds();
			double thisVal = this.toSeconds();
			if (thisVal < rval)
				return -1;
			else if (thisVal > rval)
				return 1;
			else
				return 0;
		}
		return Integer.MIN_VALUE;
	}
}
