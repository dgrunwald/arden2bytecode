package arden.runtime;

import java.lang.reflect.InvocationTargetException;

/**
 * 
 * Static helper methods.
 * 
 * @author Daniel Grunwald
 * 
 */
public final class RuntimeHelpers {
	public static ArdenValue changeTime(ArdenValue input, ArdenValue newTime) {
		if (newTime instanceof ArdenTime) {
			return input.setTime(((ArdenTime) newTime).value);
		} else {
			return input.setTime(ArdenValue.NOPRIMARYTIME);
		}
	}

	public static ArdenValue[] call(MedicalLogicModule mlm, ExecutionContext context, ArdenValue[] arguments) {
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
