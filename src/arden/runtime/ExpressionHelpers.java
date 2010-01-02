package arden.runtime;

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
}
