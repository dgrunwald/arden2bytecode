package arden.runtime;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Static helper methods for ExpressionCompiler.
 * 
 * @author Daniel Grunwald
 */
public final class ExpressionHelpers {
	/** implements the ",x" operator */
	public static ArdenList unaryComma(ArdenValue value) {
		if (value instanceof ArdenList)
			return (ArdenList) value;
		else
			return new ArdenList(new ArdenValue[] { value });
	}

	/** implements the "x,y" operator */
	public static ArdenList binaryComma(ArdenValue lhs, ArdenValue rhs) {
		ArdenValue[] left = unaryComma(lhs).values;
		ArdenValue[] right = unaryComma(rhs).values;
		ArdenValue[] result = new ArdenValue[left.length + right.length];
		System.arraycopy(left, 0, result, 0, left.length);
		System.arraycopy(right, 0, result, left.length, right.length);
		return new ArdenList(result);
	}

	/** implements the SORT DATA operator */
	public static ArdenValue sortByData(ArdenValue inputVal) {
		ArdenList input = unaryComma(inputVal);
		if (input.values.length == 0)
			return input;
		// check whether all elements are comparable (also checks whether list
		// is already sorted):
		ArdenValue lastElement = input.values[0];
		boolean alreadySorted = true;
		for (int i = 1; i < input.values.length; i++) {
			ArdenValue thisElement = input.values[i];
			int r = lastElement.compareTo(thisElement);
			if (r == Integer.MIN_VALUE) {
				// list contains non-ordered element types or invalid comparison
				return ArdenNull.INSTANCE;
			} else if (r >= 0) {
				alreadySorted = false;
			}
			lastElement = thisElement;
		}
		if (alreadySorted)
			return input;
		ArdenValue[] result = (ArdenValue[]) input.values.clone();
		Arrays.sort(result, new Comparator<ArdenValue>() {
			@Override
			public int compare(ArdenValue o1, ArdenValue o2) {
				return o1.compareTo(o2);
			};
		});
		return new ArdenList(result);
	}

	/** implements the SORT TIME operator */
	public static ArdenValue sortByTime(ArdenValue inputVal) {
		ArdenList input = unaryComma(inputVal);
		if (input.values.length == 0)
			return input;
		// check whether all elements have a primary time:
		for (ArdenValue val : input.values) {
			if (val.primaryTime == ArdenValue.NOPRIMARYTIME)
				return ArdenNull.INSTANCE;
		}
		ArdenValue[] result = (ArdenValue[]) input.values.clone();
		Arrays.sort(result, new Comparator<ArdenValue>() {
			@Override
			public int compare(ArdenValue o1, ArdenValue o2) {
				if (o1.primaryTime < o2.primaryTime)
					return -1;
				else if (o1.primaryTime > o2.primaryTime)
					return 1;
				else
					return 0;
			};
		});
		return new ArdenList(result);
	}

	/** implements the WHERE operator */
	public static ArdenValue where(ArdenValue sequence, ArdenValue condition) {
		if (condition instanceof ArdenList) {
			ArdenValue[] conditionValues = ((ArdenList) condition).values;
			int numTrue = 0;
			for (ArdenValue cond : conditionValues) {
				if (cond.isTrue())
					numTrue++;
			}
			ArdenValue[] result = new ArdenValue[numTrue];
			if (sequence instanceof ArdenList) {
				ArdenValue[] sequenceValues = ((ArdenList) sequence).values;
				if (conditionValues.length != sequenceValues.length)
					return ArdenNull.INSTANCE;
				int pos = 0;
				for (int i = 0; i < conditionValues.length; i++) {
					if (conditionValues[i].isTrue())
						result[pos++] = sequenceValues[i];
				}
				assert pos == numTrue;
			} else {
				// 1 WHERE (true, true, false) ===> (1, 1)
				for (int i = 0; i < numTrue; i++)
					result[i] = sequence;
			}
			return new ArdenList(result);
		} else {
			// condition not ArdenList:
			return condition.isTrue() ? sequence : ArdenList.EMPTY;
		}
	}

	/** implements the COUNT OF operator */
	public static ArdenValue count(ArdenValue sequence) {
		return new ArdenNumber(unaryComma(sequence).values.length);
	}

	static long getCommonTime(ArdenValue[] items) {
		if (items.length == 0)
			return ArdenValue.NOPRIMARYTIME;
		long time = items[0].primaryTime;
		for (int i = 1; i < items.length; i++) {
			if (items[i].primaryTime != time)
				return ArdenValue.NOPRIMARYTIME;
		}
		return time;
	}

	/** implements the EXIST operator */
	public static ArdenValue exist(ArdenValue sequence) {
		ArdenList input = unaryComma(sequence);
		long primaryTime = getCommonTime(input.values);
		for (ArdenValue val : input.values) {
			if (!(val instanceof ArdenNull))
				return ArdenBoolean.create(true, primaryTime);
		}
		return ArdenBoolean.create(false, primaryTime);
	}

	/** implements the SUM operator */
	public static ArdenValue sum(ArdenValue sequence) {
		ArdenList input = unaryComma(sequence);
		if (input.values.length == 0)
			return ArdenNumber.ZERO;
		ArdenValue val = input.values[0];
		for (int i = 1; i < input.values.length; i++) {
			val = BinaryOperator.ADD.runElement(val, input.values[i]);
		}
		return val;
	}

	/** implements the MEDIAN operator */
	public static ArdenValue median(ArdenValue sequence) {
		ArdenValue sorted = sortByData(sequence);
		if (!(sorted instanceof ArdenList))
			return sorted; // error during sorting
		ArdenValue[] values = ((ArdenList) sorted).values;
		if (values.length == 0) {
			return ArdenNull.INSTANCE;
		} else if ((values.length % 2) == 1) {
			return values[values.length / 2];
		} else {
			return average(binaryComma(values[values.length / 2 - 1], values[values.length / 2]));
		}
	}

	/** implements the AVERAGE operator */
	public static ArdenValue average(ArdenValue sequence) {
		ArdenValue[] values = unaryComma(sequence).values;
		if (values.length == 0)
			return ArdenNull.INSTANCE;
		if (values[0] instanceof ArdenNumber) {
			double sum = 0;
			for (ArdenValue element : values) {
				if (!(element instanceof ArdenNumber))
					return ArdenNull.INSTANCE;
				sum += ((ArdenNumber) element).value;
			}
			return ArdenNumber.create(sum / values.length, getCommonTime(values));
		} else if (values[0] instanceof ArdenTime) {
			BigInteger sum = BigInteger.ZERO;
			for (ArdenValue element : values) {
				if (!(element instanceof ArdenTime))
					return ArdenNull.INSTANCE;
				sum = sum.add(BigInteger.valueOf(((ArdenTime) element).value));
			}
			sum = sum.divide(BigInteger.valueOf(values.length));
			return new ArdenTime(sum.longValue(), getCommonTime(values));
		} else if (values[0] instanceof ArdenDuration) {
			double sum = ((ArdenDuration) values[0]).value;
			boolean isMonths = ((ArdenDuration) values[0]).isMonths;
			for (int i = 1; i < values.length; i++) {
				if (!(values[i] instanceof ArdenDuration))
					return ArdenNull.INSTANCE;
				ArdenDuration d = (ArdenDuration) values[i];
				if (isMonths && !d.isMonths) {
					isMonths = false;
					sum *= ArdenDuration.SECONDS_PER_MONTH;
				}
				if (isMonths)
					sum += d.value;
				else
					sum += d.toSeconds();
			}
			return ArdenDuration.create(sum / values.length, isMonths, getCommonTime(values));
		} else {
			return ArdenNull.INSTANCE;
		}
	}

	/** implements the VARIANCE operator */
	public static ArdenValue variance(ArdenValue sequence) {
		// unlike the other operators, VARIANCE doesn't automatically build
		// 1-element-lists
		if (!(sequence instanceof ArdenList))
			return ArdenNull.INSTANCE;
		ArdenValue[] values = ((ArdenList) sequence).values;
		if (values.length == 0)
			return ArdenNull.INSTANCE;
		double sum = 0;
		for (ArdenValue element : values) {
			if (!(element instanceof ArdenNumber))
				return ArdenNull.INSTANCE;
			sum += ((ArdenNumber) element).value;
		}
		double avg = sum / values.length;
		double diffsum = 0;
		for (ArdenValue element : values) {
			double diff = avg - ((ArdenNumber) element).value;
			diffsum += diff * diff;
		}
		double variance = diffsum / values.length;
		return ArdenNumber.create(variance, getCommonTime(values));
	}
}