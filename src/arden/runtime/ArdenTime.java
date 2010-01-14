package arden.runtime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

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

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new ArdenTime(value, newPrimaryTime);
	}

	public static final DateFormat isoDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static final DateFormat isoDateTimeFormatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	public static final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public String toString() {
		if (value % 1000 != 0) {
			return isoDateTimeFormatWithMillis.format(new Date(value));
		} else {
			return isoDateTimeFormat.format(new Date(value));
		}
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

	private long addMonths(double months) {
		int wholeMonths = (int) months;
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(value);
		c.add(GregorianCalendar.MONTH, wholeMonths);
		return c.getTimeInMillis() + (long) ((months - wholeMonths) * 1000 * ArdenDuration.SECONDS_PER_MONTH);
	}

	long add(ArdenDuration dur) {
		if (dur.isMonths) {
			return addMonths(dur.value);
		} else {
			long milliseconds = (long) (1000 * dur.value);
			return this.value + milliseconds;
		}
	}

	long subtract(ArdenDuration dur) {
		if (dur.isMonths) {
			return addMonths(-dur.value);
		} else {
			long milliseconds = (long) (1000 * dur.value);
			return this.value - milliseconds;
		}
	}
}
