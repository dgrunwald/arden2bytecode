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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Pattern;

import arden.runtime.events.AfterEvokeEvent;
import arden.runtime.events.CyclicEvokeEvent;
import arden.runtime.events.EvokeEvent;
import arden.runtime.events.NeverEvokeEvent;
import arden.runtime.events.UntilEvokeEvent;

/**
 * Static helper methods for ExpressionCompiler (mostly operators with special
 * list handling).
 * 
 * @author Daniel Grunwald
 */
public final class ExpressionHelpers {
	/** helper function */
	public static String getClassName(Object o) {
		if (o == null) {
			return "null";
		}
		return o.getClass().getSimpleName();
	}
	
	
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
		ArdenValue[] values = unaryComma(sequence).values;
		if (values.length < 2)
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
		// sum/(n-1): Bessel's correction (the spec demands the sample variance)
		double variance = diffsum / (values.length - 1);
		return ArdenNumber.create(variance, getCommonTime(values));
	}

	/** implements the IS IN operator */
	public static ArdenValue isIn(ArdenValue lhs, ArdenValue rhs) {
		ArdenValue[] list = unaryComma(rhs).values;
		if (lhs instanceof ArdenList) {
			ArdenValue[] left = ((ArdenList) lhs).values;
			ArdenValue[] result = new ArdenValue[left.length];
			for (int i = 0; i < left.length; i++)
				result[i] = isIn(left[i], list);
			return new ArdenList(result);
		} else {
			return isIn(lhs, list);
		}
	}

	private static ArdenBoolean isIn(ArdenValue lhs, ArdenValue[] list) {
		for (ArdenValue val : list) {
			if (lhs.equals(val)) {
				if (val.primaryTime == lhs.primaryTime)
					return ArdenBoolean.create(true, val.primaryTime);
				else
					return ArdenBoolean.TRUE;
			}
		}
		return ArdenBoolean.FALSE;
	}

	/** implements the SEQTO operator */
	public static ArdenValue seqto(ArdenValue lhs, ArdenValue rhs) {
		if (!(lhs instanceof ArdenNumber) || !(rhs instanceof ArdenNumber))
			return ArdenNull.INSTANCE;
		// primary times are lost (as specified)
		double lower = ((ArdenNumber) lhs).value;
		double upper = ((ArdenNumber) rhs).value;
		int lowerInt = (int) lower;
		int upperInt = (int) upper;
		if (lowerInt != lower || upperInt != upper)
			return ArdenNull.INSTANCE;
		if (lowerInt > upperInt)
			return ArdenList.EMPTY;
		ArdenValue[] result = new ArdenValue[upperInt - lowerInt + 1];
		for (int i = 0; i < result.length; i++)
			result[i] = new ArdenNumber(lowerInt + i);
		return new ArdenList(result);
	}

	/** implements the SEQTO operator */
	public static ArdenValue reverse(ArdenValue input) {
		ArdenValue[] inputs = unaryComma(input).values;
		ArdenValue[] result = new ArdenValue[inputs.length];
		for (int i = 0; i < result.length; i++)
			result[i] = inputs[inputs.length - i - 1];
		return new ArdenList(result);
	}

	/** implements the INCREASE operator */
	public static ArdenValue increase(ArdenValue input) {
		ArdenValue[] inputs = unaryComma(input).values;
		if (inputs.length == 0)
			return ArdenNull.INSTANCE;
		if (!(inputs[0] instanceof ArdenNumber || inputs[0] instanceof ArdenDuration || inputs[0] instanceof ArdenTime))
			return ArdenNull.INSTANCE;
		ArdenValue[] outputs = new ArdenValue[inputs.length - 1];
		for (int i = 0; i < outputs.length; i++) {
			if (inputs[i].getClass() != inputs[i + 1].getClass())
				return ArdenNull.INSTANCE;
			outputs[i] = BinaryOperator.SUB.runElement(inputs[i + 1], inputs[i]).setTime(inputs[i + 1].primaryTime);
		}
		return new ArdenList(outputs);
	}

	/** implements the PERCENT INCREASE operator */
	public static ArdenValue percentIncrease(ArdenValue input) {
		ArdenValue[] inputs = unaryComma(input).values;
		if (inputs.length == 0)
			return ArdenNull.INSTANCE;
		if (!(inputs[0] instanceof ArdenNumber || inputs[0] instanceof ArdenDuration))
			return ArdenNull.INSTANCE;
		ArdenValue[] outputs = new ArdenValue[inputs.length - 1];
		for (int i = 0; i < outputs.length; i++) {
			if (inputs[i].getClass() != inputs[i + 1].getClass())
				return ArdenNull.INSTANCE;
			outputs[i] = BinaryOperator.MUL.runElement(
					BinaryOperator.DIV.runElement(BinaryOperator.SUB.runElement(inputs[i + 1], inputs[i]), inputs[i]),
					ArdenNumber.ONE_HUNDRED).setTime(inputs[i + 1].primaryTime);
		}
		return new ArdenList(outputs);
	}

	/** implements the SLOPE operator */
	public static ArdenValue slope(ArdenValue input) {
		ArdenValue[] inputs = unaryComma(input).values;
		if (inputs.length < 2)
			return ArdenNull.INSTANCE;
		// linear regression through (x=primaryTime/y=value) points
		double avgX = 0;
		double avgY = 0;
		for (ArdenValue val : inputs) {
			if (!(val instanceof ArdenNumber) || val.primaryTime == ArdenValue.NOPRIMARYTIME)
				return ArdenNull.INSTANCE;
			avgX += val.primaryTime / 86000000.0; // x in days
			avgY += ((ArdenNumber) val).value;
		}
		avgX /= inputs.length;
		avgY /= inputs.length;
		double z = 0;
		double n = 0;
		for (ArdenValue val : inputs) {
			double x = val.primaryTime / 86000000.0;
			double y = ((ArdenNumber) val).value;
			z += (x - avgX) * (y - avgY);
			n += (x - avgX) * (x - avgX);
		}
		return ArdenNumber.create(z / n, ArdenValue.NOPRIMARYTIME);
	}

	/** Implements the IS LIST operator. */
	public static ArdenBoolean isList(ArdenValue input) {
		if (input instanceof ArdenList) {
			return ArdenBoolean.create(true, getCommonTime(((ArdenList) input).values));
		} else {
			return ArdenBoolean.create(false, input.primaryTime);
		}
	}

	/** Implements the FIRST aggregation operator. */
	public static ArdenValue first(ArdenValue input) {
		ArdenValue[] arr = unaryComma(input).values;
		if (arr.length == 0)
			return ArdenNull.INSTANCE;
		else
			return arr[0];
	}

	/** Implements the LAST aggregation operator. */
	public static ArdenValue last(ArdenValue input) {
		ArdenValue[] arr = unaryComma(input).values;
		if (arr.length == 0)
			return ArdenNull.INSTANCE;
		else
			return arr[arr.length - 1];
	}

	/** Implements the FIRST transformation operator. */
	public static ArdenValue first(ArdenValue input, int numberOfElements) {
		ArdenList inputList = unaryComma(input);
		if (numberOfElements >= inputList.values.length)
			return inputList;
		ArdenValue[] result = new ArdenValue[numberOfElements];
		System.arraycopy(inputList.values, 0, result, 0, numberOfElements);
		return new ArdenList(result);
	}

	/** Implements the LAST transformation operator. */
	public static ArdenValue last(ArdenValue input, int numberOfElements) {
		ArdenList inputList = unaryComma(input);
		if (numberOfElements >= inputList.values.length)
			return inputList;
		ArdenValue[] result = new ArdenValue[numberOfElements];
		System.arraycopy(inputList.values, inputList.values.length - numberOfElements, result, 0, numberOfElements);
		return new ArdenList(result);
	}

	/** Implements the INDEX MINIMUM aggregation operator. */
	public static ArdenValue indexMinimum(ArdenValue input) {
		ArdenValue[] arr = unaryComma(input).values;
		if (arr.length == 0)
			return ArdenNull.INSTANCE;
		int min = 0;
		for (int i = 1; i < arr.length; i++) {
			int r = arr[min].compareTo(arr[i]);
			if (r == Integer.MIN_VALUE)
				return ArdenNull.INSTANCE;
			if (r == 1 || (r == 0 && arr[i].primaryTime > arr[min].primaryTime))
				min = i;
		}
		return ArdenNumber.create(min + 1, arr[min].primaryTime);
	}

	/** Implements the INDEX MINIMUM transformation operator. */
	public static ArdenValue indexMinimum(ArdenValue input, int numberOfElements) {
		ArdenValue[] arr = unaryComma(input).values;
		ArdenValue sortedInput = sortByData(input);
		if (!(sortedInput instanceof ArdenList))
			return ArdenNull.INSTANCE;
		if (numberOfElements > arr.length)
			numberOfElements = arr.length;
		if (numberOfElements == 0)
			return ArdenList.EMPTY;
		ArdenValue[] output = new ArdenValue[numberOfElements];
		ArdenValue pivot = ((ArdenList) sortedInput).values[numberOfElements - 1];
		int pos = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].compareTo(pivot) <= 0) {
				output[pos++] = ArdenNumber.create(i + 1, ArdenValue.NOPRIMARYTIME);
				if (pos == numberOfElements)
					break;
			}
		}
		if (pos != numberOfElements)
			throw new Error("algorithm error.");
		return new ArdenList(output);
	}

	/** Implements the INDEX MAXIMUM aggregation operator. */
	public static ArdenValue indexMaximum(ArdenValue input) {
		ArdenValue[] arr = unaryComma(input).values;
		if (arr.length == 0)
			return ArdenNull.INSTANCE;
		int max = 0;
		for (int i = 1; i < arr.length; i++) {
			int r = arr[max].compareTo(arr[i]);
			if (r == Integer.MIN_VALUE)
				return ArdenNull.INSTANCE;
			if (r == -1 || (r == 0 && arr[i].primaryTime > arr[max].primaryTime))
				max = i;
		}
		return ArdenNumber.create(max + 1, arr[max].primaryTime);
	}

	/** Implements the INDEX MAXIMUM transformation operator. */
	public static ArdenValue indexMaximum(ArdenValue input, int numberOfElements) {
		ArdenValue[] arr = unaryComma(input).values;
		ArdenValue sortedInput = sortByData(input);
		if (!(sortedInput instanceof ArdenList))
			return ArdenNull.INSTANCE;
		if (numberOfElements > arr.length)
			numberOfElements = arr.length;
		if (numberOfElements == 0)
			return ArdenList.EMPTY;
		ArdenValue[] output = new ArdenValue[numberOfElements];
		ArdenValue pivot = ((ArdenList) sortedInput).values[arr.length - numberOfElements];
		int pos = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].compareTo(pivot) >= 0) {
				output[pos++] = ArdenNumber.create(i + 1, ArdenValue.NOPRIMARYTIME);
				if (pos == numberOfElements)
					break;
			}
		}
		if (pos != numberOfElements)
			throw new Error("algorithm error.");
		return new ArdenList(output);
	}

	/** Implements the INDEX EARLIEST aggregation operator. */
	public static ArdenValue indexEarliest(ArdenValue input) {
		ArdenValue[] arr = unaryComma(input).values;
		if (arr.length == 0 || arr[0].primaryTime == ArdenValue.NOPRIMARYTIME)
			return ArdenNull.INSTANCE;
		int best = 0;
		for (int i = 1; i < arr.length; i++) {
			if (arr[i].primaryTime == ArdenValue.NOPRIMARYTIME)
				return ArdenNull.INSTANCE;
			if (arr[i].primaryTime < arr[best].primaryTime)
				best = i;
		}
		return ArdenNumber.create(best + 1, arr[best].primaryTime);
	}

	/** Implements the INDEX EARLIEST transformation operator. */
	public static ArdenValue indexEarliest(ArdenValue input, int numberOfElements) {
		ArdenValue[] arr = unaryComma(input).values;
		ArdenValue sortedInput = sortByTime(input);
		if (!(sortedInput instanceof ArdenList))
			return ArdenNull.INSTANCE;
		if (numberOfElements > arr.length)
			numberOfElements = arr.length;
		if (numberOfElements == 0)
			return ArdenList.EMPTY;
		ArdenValue[] output = new ArdenValue[numberOfElements];
		ArdenValue pivot = ((ArdenList) sortedInput).values[numberOfElements - 1];
		int pos = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].primaryTime <= pivot.primaryTime) {
				output[pos++] = ArdenNumber.create(i + 1, ArdenValue.NOPRIMARYTIME);
				if (pos == numberOfElements)
					break;
			}
		}
		if (pos != numberOfElements)
			throw new Error("algorithm error.");
		return new ArdenList(output);
	}

	/** Implements the INDEX LATEST aggregation operator. */
	public static ArdenValue indexLatest(ArdenValue input) {
		ArdenValue[] arr = unaryComma(input).values;
		if (arr.length == 0 || arr[0].primaryTime == ArdenValue.NOPRIMARYTIME)
			return ArdenNull.INSTANCE;
		int best = 0;
		for (int i = 1; i < arr.length; i++) {
			if (arr[i].primaryTime == ArdenValue.NOPRIMARYTIME)
				return ArdenNull.INSTANCE;
			if (arr[i].primaryTime > arr[best].primaryTime)
				best = i;
		}
		return ArdenNumber.create(best + 1, arr[best].primaryTime);
	}

	/** Implements the INDEX LATEST transformation operator. */
	public static ArdenValue indexLatest(ArdenValue input, int numberOfElements) {
		ArdenValue[] arr = unaryComma(input).values;
		ArdenValue sortedInput = sortByTime(input);
		if (!(sortedInput instanceof ArdenList))
			return ArdenNull.INSTANCE;
		if (numberOfElements > arr.length)
			numberOfElements = arr.length;
		if (numberOfElements == 0)
			return ArdenList.EMPTY;
		ArdenValue[] output = new ArdenValue[numberOfElements];
		ArdenValue pivot = ((ArdenList) sortedInput).values[arr.length - numberOfElements];
		int pos = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].primaryTime <= pivot.primaryTime) {
				output[pos++] = ArdenNumber.create(i + 1, ArdenValue.NOPRIMARYTIME);
				if (pos == numberOfElements)
					break;
			}
		}
		if (pos != numberOfElements)
			throw new Error("algorithm error.");
		return new ArdenList(output);
	}

	/** Implements the INDEX NEAREST operator. */
	public static ArdenValue indexNearest(ArdenValue time, ArdenValue input) {
		ArdenValue[] arr = unaryComma(input).values;
		if (arr.length == 0 || arr[0].primaryTime == ArdenValue.NOPRIMARYTIME || !(time instanceof ArdenTime))
			return ArdenNull.INSTANCE;
		long inputTime = ((ArdenTime) time).value;
		int bestIndex = 0;
		long bestDiff = Math.abs(arr[0].primaryTime - inputTime);
		for (int i = 1; i < arr.length; i++) {
			if (arr[i].primaryTime == ArdenValue.NOPRIMARYTIME)
				return ArdenNull.INSTANCE;
			long diff = Math.abs(arr[i].primaryTime - inputTime);
			if (diff < bestDiff) {
				bestDiff = diff;
				bestIndex = i;
			}
		}
		return ArdenNumber.create(bestIndex + 1, arr[bestIndex].primaryTime);
	}

	/** implements the [] (element) operator */
	public static ArdenValue elementAt(ArdenValue list, ArdenValue index) {
		ArdenValue[] values = unaryComma(list).values;
		if (index instanceof ArdenList) {
			ArdenValue[] indices = ((ArdenList) index).values;
			ArdenValue[] result = new ArdenValue[indices.length];
			for (int i = 0; i < indices.length; i++) {
				int val = RuntimeHelpers.getPrimitiveIntegerValue(indices[i]);
				if (val < 1 || val > values.length)
					result[i] = ArdenNull.INSTANCE;
				else
					result[i] = values[val - 1];
			}
			return new ArdenList(result);
		} else {
			int val = RuntimeHelpers.getPrimitiveIntegerValue(index);
			if (val < 1 || val > values.length)
				return ArdenNull.INSTANCE;
			return values[val - 1];
		}
	}

	/** implements the ANY operator */
	public static ArdenValue any(ArdenValue sequence) {
		ArdenList input = unaryComma(sequence);
		long primaryTime = getCommonTime(input.values);
		boolean allFalse = true;
		for (ArdenValue val : input.values) {
			if (val.isTrue())
				return ArdenBoolean.create(true, primaryTime);
			allFalse &= val.isFalse();
		}
		if (allFalse)
			return ArdenBoolean.create(false, primaryTime);
		else
			return ArdenNull.create(primaryTime);
	}

	/** implements the ALL operator */
	public static ArdenValue all(ArdenValue sequence) {
		ArdenList input = unaryComma(sequence);
		long primaryTime = getCommonTime(input.values);
		boolean allTrue = true;
		for (ArdenValue val : input.values) {
			if (val.isFalse())
				return ArdenBoolean.create(false, primaryTime);
			allTrue &= val.isTrue();
		}
		if (allTrue)
			return ArdenBoolean.create(true, primaryTime);
		else
			return ArdenNull.create(primaryTime);
	}
	
	/** implements the AFTER duration operator */
	public static EvokeEvent after(ArdenValue duration, EvokeEvent event) {
		if (duration instanceof ArdenDuration) {
			long primaryTime = getCommonTime(new ArdenValue[]{duration, event});
			return new AfterEvokeEvent((ArdenDuration) duration, event, primaryTime);
		}
		throw new RuntimeException("AFTER operator not implemented for "
						+ getClassName(duration) 
						+ " AFTER " + getClassName(event));
	}
	
	public static EvokeEvent timeOf(EvokeEvent event, ExecutionContext context) {
		return event;
	}
	
	public static ArdenValue createDuration(ArdenValue val, double multiplier, boolean isMonths) {
		if (val instanceof ArdenList) {
			ArdenValue[] inputs = ((ArdenList) val).values;
			ArdenValue[] results = new ArdenValue[inputs.length];
			for (int i = 0; i < inputs.length; i++)
				results[i] = createDuration(inputs[i], multiplier, isMonths);
			return new ArdenList(results);
		} else if (val instanceof ArdenNumber) {
			return ArdenDuration.create(((ArdenNumber) val).value * multiplier, isMonths, val.primaryTime);
		} else {
			return ArdenNull.create(val.primaryTime);
		}
	}
	
	public static EvokeEvent createEvokeCycle(ArdenValue interval, ArdenValue length, EvokeEvent start, ExecutionContext context) {
		if (interval instanceof ArdenDuration && length instanceof ArdenDuration) {
			ArdenTime starting = start.getNextRunTime(context);
			if (starting != null) {
				return new CyclicEvokeEvent((ArdenDuration) interval, (ArdenDuration) length, starting);
			} else {
				return new NeverEvokeEvent();
			}
		}
		throw new RuntimeException("cannot create evoke cycle with these types: " + 
						getClassName(interval) + ", " +
						getClassName(length) + ", " +
						getClassName(start));
	}

	public static EvokeEvent until(EvokeEvent simpleEvokeCycle, ArdenValue untilExpr) {
		if (untilExpr instanceof ArdenTime) {
			return new UntilEvokeEvent(simpleEvokeCycle, (ArdenTime) untilExpr);
		} else if (untilExpr instanceof ArdenNull) {
			return simpleEvokeCycle;
		}
		throw new RuntimeException("cannot create until event for type " + getClassName(untilExpr) + " after the 'until'");
	}
	
	/** returns the EvokeEvent when 'call' is stated in the evoke slot */
	public static EvokeEvent evokeSlotCall() {
		return new NeverEvokeEvent();
	}
	
	public static ArdenValue extractTimeComponent(ArdenValue time, int component) {
		if (time instanceof ArdenList) {
			ArdenValue[] inputs = ((ArdenList) time).values;
			ArdenValue[] results = new ArdenValue[inputs.length];
			for (int i = 0; i < inputs.length; i++)
				results[i] = extractTimeComponent(inputs[i], component);
			return new ArdenList(results);
		} else if (time instanceof ArdenTime) {
			GregorianCalendar c = new GregorianCalendar();
			c.setTimeInMillis(((ArdenTime) time).value);
			int val = c.get(component);
			if (component == GregorianCalendar.MONTH)
				val++;
			return ArdenNumber.create(val, time.primaryTime);
		} else {
			return ArdenNull.create(time.primaryTime);
		}
	}

	/** Implements the string concatenation operator || */
	public static ArdenString concat(ArdenValue lhs, ArdenValue rhs) {
		return new ArdenString(toString(lhs) + toString(rhs));
	}

	/** Converts a single value to string. */
	public static String toString(ArdenValue val) {
		if (val instanceof ArdenString)
			return ((ArdenString) val).value;
		else
			return val.toString();
	}

	/** implements the STRING... operator */
	public static ArdenString joinString(ArdenValue input) {
		StringBuilder b = new StringBuilder();
		for (ArdenValue val : unaryComma(input).values) {
			b.append(toString(val));
		}
		return new ArdenString(b.toString());
	}

	/** implements the EXTRACT CHARACTERS operator */
	public static ArdenList extractCharacters(ArdenValue input) {
		ArrayList<String> strings = new ArrayList<String>();
		for (ArdenValue val : unaryComma(input).values) {
			String txt = toString(val);
			for (int i = 0; i < txt.length(); i++)
				strings.add(txt.substring(i, i + 1));
		}
		ArdenValue[] result = new ArdenValue[strings.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = new ArdenString(strings.get(i));
		return new ArdenList(result);
	}

	/** implements the TRIM operator */
	public static ArdenValue trim(ArdenValue input) {
		if (input instanceof ArdenList) {
			ArdenValue[] list = ((ArdenList) input).values;
			if (list.length == 0)
				return ArdenNull.INSTANCE; // special case
			ArdenValue[] results = new ArdenValue[list.length];
			for (int i = 0; i < list.length; i++)
				results[i] = trim(list[i]);
			return new ArdenList(results);
		} else if (input instanceof ArdenString) {
			return new ArdenString(((ArdenString) input).value.trim(), input.primaryTime);
		} else {
			return ArdenNull.create(input.primaryTime);
		}
	}

	/** implements the TRIM LEFT operator */
	public static ArdenValue trimLeft(ArdenValue input) {
		if (input instanceof ArdenList) {
			ArdenValue[] list = ((ArdenList) input).values;
			if (list.length == 0)
				return ArdenNull.INSTANCE; // special case
			ArdenValue[] results = new ArdenValue[list.length];
			for (int i = 0; i < list.length; i++)
				results[i] = trimLeft(list[i]);
			return new ArdenList(results);
		} else if (input instanceof ArdenString) {
			String str = ((ArdenString) input).value;
			int index = 0;
			while (index < str.length() && Character.isWhitespace(str.charAt(index)))
				index++;
			return new ArdenString(str.substring(index), input.primaryTime);
		} else {
			return ArdenNull.create(input.primaryTime);
		}
	}

	/** implements the TRIM RIGHT operator */
	public static ArdenValue trimRight(ArdenValue input) {
		if (input instanceof ArdenList) {
			ArdenValue[] list = ((ArdenList) input).values;
			if (list.length == 0)
				return ArdenNull.INSTANCE; // special case
			ArdenValue[] results = new ArdenValue[list.length];
			for (int i = 0; i < list.length; i++)
				results[i] = trimRight(list[i]);
			return new ArdenList(results);
		} else if (input instanceof ArdenString) {
			String str = ((ArdenString) input).value;
			int index = str.length();
			while (index > 0 && Character.isWhitespace(str.charAt(index - 1)))
				index--;
			return new ArdenString(str.substring(0, index), input.primaryTime);
		} else {
			return ArdenNull.create(input.primaryTime);
		}
	}

	/** implements the LENGTH OF operator */
	public static ArdenValue length(ArdenValue input) {
		if (input instanceof ArdenList) {
			ArdenValue[] list = ((ArdenList) input).values;
			if (list.length == 0)
				return ArdenNull.INSTANCE; // special case
			ArdenValue[] results = new ArdenValue[list.length];
			for (int i = 0; i < list.length; i++)
				results[i] = length(list[i]);
			return new ArdenList(results);
		} else if (input instanceof ArdenString) {
			String str = ((ArdenString) input).value;
			return ArdenNumber.create(str.length(), ArdenValue.NOPRIMARYTIME);
		} else {
			return ArdenNull.INSTANCE;
		}
	}

	/** implements the UPPERCASE operator */
	public static ArdenValue toUpperCase(ArdenValue input) {
		if (input instanceof ArdenList) {
			ArdenValue[] list = ((ArdenList) input).values;
			if (list.length == 0)
				return ArdenNull.INSTANCE; // special case
			ArdenValue[] results = new ArdenValue[list.length];
			for (int i = 0; i < list.length; i++)
				results[i] = toUpperCase(list[i]);
			return new ArdenList(results);
		} else if (input instanceof ArdenString) {
			String str = ((ArdenString) input).value;
			return new ArdenString(str.toUpperCase(), input.primaryTime);
		} else {
			return ArdenNull.create(input.primaryTime);
		}
	}

	/** implements the LOWERCASE operator */
	public static ArdenValue toLowerCase(ArdenValue input) {
		if (input instanceof ArdenList) {
			ArdenValue[] list = ((ArdenList) input).values;
			if (list.length == 0)
				return ArdenNull.INSTANCE; // special case
			ArdenValue[] results = new ArdenValue[list.length];
			for (int i = 0; i < list.length; i++)
				results[i] = toLowerCase(list[i]);
			return new ArdenList(results);
		} else if (input instanceof ArdenString) {
			String str = ((ArdenString) input).value;
			return new ArdenString(str.toLowerCase(), input.primaryTime);
		} else {
			return ArdenNull.create(input.primaryTime);
		}
	}

	/** implements the MATCHES PATTERN operator */
	public static ArdenValue matchesPattern(ArdenValue lhs, ArdenValue rhs) {
		Pattern pattern = createPattern(rhs);
		if (lhs instanceof ArdenString) {
			String input = ((ArdenString) lhs).value;
			return pattern.matcher(input).matches() ? ArdenBoolean.TRUE : ArdenBoolean.FALSE;
		} else if (lhs instanceof ArdenList) {
			ArdenValue[] inputs = ((ArdenList) lhs).values;
			ArdenValue[] results = new ArdenValue[inputs.length];
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] instanceof ArdenString) {
					String input = ((ArdenString) inputs[i]).value;
					results[i] = pattern.matcher(input).matches() ? ArdenBoolean.TRUE : ArdenBoolean.FALSE;
				} else {
					results[i] = ArdenNull.INSTANCE;
				}
			}
			return new ArdenList(results);
		} else {
			return ArdenNull.INSTANCE;
		}
	}

	/** helper for MATCHES PATTERN implementation */
	private static Pattern createPattern(ArdenValue rhs) {
		if (rhs instanceof ArdenString) {
			String pattern = ((ArdenString) rhs).value;
			StringBuilder regex = new StringBuilder();
			int processingEndOffset = 0;
			regex.append('^');
			for (int i = 0; i < pattern.length(); i++) {
				char c = pattern.charAt(i);
				if (c == '_' || c == '%') {
					if (processingEndOffset < i)
						regex.append(Pattern.quote(pattern.substring(processingEndOffset, i)));
					regex.append('.');
					if (c == '%')
						regex.append('*');
					processingEndOffset = i + 1;
				} else if (c == '\\') {
					if (processingEndOffset < i)
						regex.append(Pattern.quote(pattern.substring(processingEndOffset, i)));
					processingEndOffset = i + 1; // don't output the \ itself
					i++; // skip processing the character after the \, thus
					// copying it to the output escaped
				}
			}
			if (processingEndOffset < pattern.length())
				regex.append(Pattern.quote(pattern.substring(processingEndOffset, pattern.length())));
			regex.append('$');
			return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		} else {
			return null;
		}
	}

	/** CLONE operator implementation */
	public static ArdenValue cloneObjects(ArdenValue input) {
		return cloneObjects(input, new HashMap<ArdenObject, ArdenObject>());
	}

	private static ArdenValue cloneObjects(ArdenValue input, HashMap<ArdenObject, ArdenObject> objectMap) {
		if (input instanceof ArdenList) {
			ArdenValue[] inputs = ((ArdenList) input).values;
			ArdenValue[] results = new ArdenValue[inputs.length];
			for (int i = 0; i < inputs.length; i++)
				results[i] = cloneObjects(inputs[i], objectMap);
			return new ArdenList(results);
		} else if (input instanceof ArdenObject) {
			ArdenObject oldObj = (ArdenObject) input;
			ArdenObject newObj = objectMap.get(oldObj);
			if (newObj == null) {
				newObj = new ArdenObject(oldObj.type);
				objectMap.put(oldObj, newObj); // store mapping
				for (int i = 0; i < oldObj.fields.length; i++)
					newObj.fields[i] = cloneObjects(oldObj.fields[i], objectMap);
			}
			return newObj;
		} else {
			return input;
		}
	}

	/** EXTRACT ATTRIBUTE NAMES operator implementation */
	public static ArdenValue extractAttributeNames(ArdenValue input) {
		ObjectType type;
		if (input instanceof ArdenObject) {
			// get type of object
			type = ((ArdenObject) input).type;
		} else if (input instanceof ArdenList) {
			// if all objects in list have the same type, get that type
			ArdenValue[] inputs = ((ArdenList) input).values;
			if (inputs.length == 0 || !(inputs[0] instanceof ArdenObject))
				return ArdenNull.INSTANCE;
			type = ((ArdenObject) inputs[0]).type;
			for (int i = 1; i < inputs.length; i++) {
				if (!(inputs[i] instanceof ArdenObject))
					return ArdenNull.INSTANCE;
				if (((ArdenObject) inputs[i]).type != type)
					return ArdenNull.INSTANCE;
			}
		} else {
			return ArdenNull.INSTANCE;
		}
		ArdenValue[] attributeNames = new ArdenValue[type.fieldNames.length];
		for (int i = 0; i < attributeNames.length; i++)
			attributeNames[i] = new ArdenString(type.fieldNames[i]);
		return new ArdenList(attributeNames);
	}

	/** IS <Object-Type> operator implementation */
	public static ArdenValue isObjectType(ArdenValue input, ObjectType type) {
		if (input instanceof ArdenList) {
			ArdenValue[] inputs = ((ArdenList) input).values;
			ArdenValue[] results = new ArdenValue[inputs.length];
			for (int i = 0; i < inputs.length; i++)
				results[i] = isObjectType(inputs[i], type);
			return new ArdenList(results);
		} else if (input instanceof ArdenObject) {
			return ArdenBoolean.create(((ArdenObject) input).type == type, input.primaryTime);
		} else {
			return ArdenBoolean.create(false, input.primaryTime);
		}
	}
}
