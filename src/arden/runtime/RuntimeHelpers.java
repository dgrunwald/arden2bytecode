package arden.runtime;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Static helper methods.
 * 
 * @author Daniel Grunwald
 */
public final class RuntimeHelpers {
	public static ArdenValue changeTime(ArdenValue input, ArdenValue newTime) {
		if (newTime instanceof ArdenTime) {
			return input.setTime(((ArdenTime) newTime).value);
		} else {
			return input.setTime(ArdenValue.NOPRIMARYTIME);
		}
	}

	public static ArdenValue[] call(ArdenRunnable mlm, ExecutionContext context, ArdenValue[] arguments) {
		try {
			return mlm.run(context, arguments);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static final double DEFAULT_URGENCY = 50;

	public static double urgencyGetPrimitiveValue(ArdenValue val) {
		if (val instanceof ArdenNumber)
			return ((ArdenNumber) val).value;
		else
			return DEFAULT_URGENCY;
	}

	/** Converts val to int. Returns -1 if val is not an integer. */
	public static int getPrimitiveIntegerValue(ArdenValue val) {
		if (!(val instanceof ArdenNumber))
			return -1;
		double v = ((ArdenNumber) val).value;
		int i = (int) v;
		if (i == v)
			return i;
		else
			return -1;
	}

	/**
	 * Converts integer values into a string representing the character code.
	 * Used for %c format specification.
	 */
	public static String formatCharacter(ArdenValue val) {
		int num = getPrimitiveIntegerValue(val);
		if (num < 0 || num > Character.MAX_VALUE) {
			return "";
		} else {
			return Character.toString((char) num);
		}
	}

	public static String limitStringLength(String input, int maxLength) {
		if (input.length() > maxLength)
			return input.substring(0, maxLength);
		else
			return input;
	}

	/** Helper method for several format specifications. */
	public static String formatNumber(ArdenValue val, NumberFormat format) {
		if (val instanceof ArdenNumber) {
			return format.format(((ArdenNumber) val).value);
		} else {
			return ExpressionHelpers.toString(val);
		}
	}

	/** Helper method for %t format specification. */
	public static String formatTime(ArdenValue val, int precision) {
		if (val instanceof ArdenTime) {
			long time = ((ArdenTime) val).value;
			switch (precision) {
			case 0: {
				GregorianCalendar c = new GregorianCalendar();
				c.setTimeInMillis(time);
				return Integer.toString(c.get(Calendar.YEAR));
			}
			case 1: {
				GregorianCalendar c = new GregorianCalendar();
				c.setTimeInMillis(time);
				return Integer.toString(c.get(Calendar.YEAR)) + "-" + Integer.toString(c.get(Calendar.MONTH) + 1);
			}
			case 2:
				return DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(time));
			case 3:
				GregorianCalendar c = new GregorianCalendar();
				c.setTimeInMillis(time);
				return DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(time)) + " "
						+ Integer.toString(c.get(Calendar.HOUR_OF_DAY)) + "h";
			case 4:
				return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(time));
			default:
				return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(new Date(time));
			}
		} else {
			return ExpressionHelpers.toString(val);
		}
	}

	public static String padRight(String input, int length, char padChar) {
		StringBuilder b = new StringBuilder(length);
		b.append(input);
		while (b.length() < length)
			b.append(padChar);
		return b.toString();
	}

	public static String padLeft(String input, int length, char padChar) {
		StringBuilder b = new StringBuilder(length);
		while (b.length() + input.length() < length)
			b.append(padChar);
		b.append(input);
		return b.toString();
	}

	public static DatabaseQuery constrainQueryWithinTo(DatabaseQuery q, ArdenValue start, ArdenValue end) {
		if (start instanceof ArdenTime && end instanceof ArdenTime)
			return q.occursWithinTo((ArdenTime) start, (ArdenTime) end);
		else
			return DatabaseQuery.NULL;
	}

	public static DatabaseQuery constrainQueryNotWithinTo(DatabaseQuery q, ArdenValue start, ArdenValue end) {
		if (start instanceof ArdenTime && end instanceof ArdenTime)
			return q.occursNotWithinTo((ArdenTime) start, (ArdenTime) end);
		else
			return DatabaseQuery.NULL;
	}

	public static DatabaseQuery constrainQueryBefore(DatabaseQuery q, ArdenValue time) {
		if (time instanceof ArdenTime)
			return q.occursBefore((ArdenTime) time);
		else
			return DatabaseQuery.NULL;
	}

	public static DatabaseQuery constrainQueryNotBefore(DatabaseQuery q, ArdenValue time) {
		if (time instanceof ArdenTime)
			return q.occursNotBefore((ArdenTime) time);
		else
			return DatabaseQuery.NULL;
	}

	public static DatabaseQuery constrainQueryAfter(DatabaseQuery q, ArdenValue time) {
		if (time instanceof ArdenTime)
			return q.occursAfter((ArdenTime) time);
		else
			return DatabaseQuery.NULL;
	}

	public static DatabaseQuery constrainQueryNotAfter(DatabaseQuery q, ArdenValue time) {
		if (time instanceof ArdenTime)
			return q.occursNotAfter((ArdenTime) time);
		else
			return DatabaseQuery.NULL;
	}
}
