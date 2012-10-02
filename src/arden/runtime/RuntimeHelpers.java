// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
	
	public static final double DEFAULT_PRIORITY = 50; 

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

	public static ArdenValue getStartOfDay(ArdenValue time) {
		if (time instanceof ArdenTime) {
			GregorianCalendar c = new GregorianCalendar();
			c.setTimeInMillis(((ArdenTime) time).value);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			return new ArdenTime(c.getTimeInMillis(), time.primaryTime);
		} else {
			return ArdenNull.create(time.primaryTime);
		}
	}

	public static ArdenValue getEndOfDay(ArdenValue time) {
		if (time instanceof ArdenTime) {
			GregorianCalendar c = new GregorianCalendar();
			c.setTimeInMillis(((ArdenTime) time).value);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			c.set(Calendar.MILLISECOND, 999);
			return new ArdenTime(c.getTimeInMillis(), time.primaryTime);
		} else {
			return ArdenNull.create(time.primaryTime);
		}
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

	public static DatabaseQuery constrainQueryWithinSurrounding(DatabaseQuery q, ArdenValue duration, ArdenValue time) {
		// within DURATION surrounding TIME
		return constrainQueryWithinTo(q, BinaryOperator.BEFORE.run(duration, time), BinaryOperator.AFTER.run(duration,
				time));
	}

	public static DatabaseQuery constrainQueryNotWithinSurrounding(DatabaseQuery q, ArdenValue duration, ArdenValue time) {
		// NOT within DURATION surrounding TIME
		return constrainQueryNotWithinTo(q, BinaryOperator.BEFORE.run(duration, time), BinaryOperator.AFTER.run(
				duration, time));
	}

	public static DatabaseQuery constrainQueryWithinSameDay(DatabaseQuery q, ArdenValue time) {
		return constrainQueryWithinTo(q, getStartOfDay(time), getEndOfDay(time));
	}

	public static DatabaseQuery constrainQueryNotWithinSameDay(DatabaseQuery q, ArdenValue time) {
		return constrainQueryNotWithinTo(q, getStartOfDay(time), getEndOfDay(time));
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

	public static DatabaseQuery constrainQueryAt(DatabaseQuery q, ArdenValue time) {
		if (time instanceof ArdenTime)
			return q.occursAt((ArdenTime) time);
		else
			return DatabaseQuery.NULL;
	}

	public static DatabaseQuery constrainQueryNotAt(DatabaseQuery q, ArdenValue time) {
		if (time instanceof ArdenTime)
			return q.occursNotAt((ArdenTime) time);
		else
			return DatabaseQuery.NULL;
	}

	public static ArdenValue readAs(ArdenValue[] inputs, ObjectType type) {
		if (type == null || inputs == null || inputs.length == 0)
			return ArdenNull.INSTANCE;
		boolean allInputsAreLists = true;
		int shortestListLength = Integer.MAX_VALUE;
		for (ArdenValue input : inputs) {
			if (input instanceof ArdenList)
				shortestListLength = Math.min(shortestListLength, ((ArdenList) input).values.length);
			else
				allInputsAreLists = false;
		}
		if (allInputsAreLists) {
			ArdenValue[] results = new ArdenValue[shortestListLength];
			for (int i = 0; i < results.length; i++) {
				ArdenObject obj = new ArdenObject(type);
				for (int j = 0; j < inputs.length && j < obj.fields.length; j++)
					obj.fields[j] = ((ArdenList) inputs[j]).values[i];
				results[i] = obj;
			}
			return new ArdenList(results);
		} else {
			ArdenObject obj = new ArdenObject(type);
			for (int j = 0; j < inputs.length && j < obj.fields.length; j++)
				obj.fields[j] = inputs[j];
			return obj;
		}
	}

	public static ArdenValue getObjectMember(ArdenValue objref, String upperCaseFieldName) {
		if (objref instanceof ArdenObject) {
			ArdenObject obj = (ArdenObject) objref;
			int index = obj.type.getFieldIndex(upperCaseFieldName);
			if (index < 0)
				return ArdenNull.INSTANCE;
			else
				return obj.fields[index];
		} else if (objref instanceof ArdenList) {
			ArdenValue[] inputs = ((ArdenList) objref).values;
			ArdenValue[] results = new ArdenValue[inputs.length];
			for (int i = 0; i < inputs.length; i++)
				results[i] = getObjectMember(inputs[i], upperCaseFieldName);
			return new ArdenList(results);
		} else {
			return ArdenNull.INSTANCE;
		}
	}

	public static void setObjectMember(ArdenValue objref, String upperCaseFieldName, ArdenValue newValue) {
		if (objref instanceof ArdenObject) {
			ArdenObject obj = (ArdenObject) objref;
			int index = obj.type.getFieldIndex(upperCaseFieldName);
			if (index >= 0)
				obj.fields[index] = newValue;
		} else if (objref instanceof ArdenList) {
			for (ArdenValue listEntry : ((ArdenList) objref).values) {
				setObjectMember(listEntry, upperCaseFieldName, newValue);
			}
		}
	}
}
