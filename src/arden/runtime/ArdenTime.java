package arden.runtime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class ArdenTime extends ArdenValue {
	/** Number of milliseconds since 1.1.1970, midnight GMT */
	public final long value;

	public ArdenTime(long value) {
		this.value = value;
	}

	public ArdenTime(Date value) {
		this.value = value.getTime();
	}

	public ArdenTime(long value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	public static final DateFormat isoDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public String toString() {
		return isoDateTimeFormat.format(new Date(value)) + primaryTimeToString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ArdenTime) && (value == ((ArdenNumber) obj).value);
	}

	@Override
	public int hashCode() {
		return new Long(value).hashCode();
	}

	@Override
	public int compareTo(ArdenValue rhs) {
		if (rhs instanceof ArdenTime) {
			long rval = ((ArdenTime) rhs).value;
			if (value < rval)
				return -1;
			else if (value > rval)
				return 1;
			else
				return 0;
		}
		return Integer.MIN_VALUE;
	}

	long addMonths(double months) {
		// TODO: implement this
		throw new RuntimeException("time+month not implemented");
	}
}
