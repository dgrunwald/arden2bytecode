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

/** Operators of the form '<n:type> := <n:type> op1 <n:type> op2 <n:type>' */
public abstract class TernaryOperator {
	private final String name;

	/**
	 * Runs the operator for a single element (all parameters must be non-lists)
	 */
	public abstract ArdenValue runElement(ArdenValue arg1, ArdenValue arg2, ArdenValue arg3);

	public static final TernaryOperator WITHINTO = new TernaryOperator("WITHINTO") {
		// arg1 IS WITHIN arg2 TO arg3
		@Override
		public ArdenValue runElement(ArdenValue arg1, ArdenValue arg2, ArdenValue arg3) {
			long newTime = combinePrimaryTime(arg1.primaryTime, arg2.primaryTime, arg3.primaryTime);
			int cmp1 = arg1.compareTo(arg2);
			int cmp2 = arg1.compareTo(arg3);
			if (cmp1 == Integer.MIN_VALUE || cmp2 == Integer.MIN_VALUE)
				return ArdenNull.create(newTime);
			return ArdenBoolean.create((cmp1 * cmp2) <= 0, newTime);
		};
	};

	public static final TernaryOperator WITHINSURROUNDING = new TernaryOperator("WITHINSURROUNDING") {
		// <n:time> IS WITHIN <n:duration> SURROUNDING <n:time>
		// => argument IS WITHIN (dur BEFORE time) TO (dur AFTER time)
		@Override
		public ArdenValue runElement(ArdenValue arg1, ArdenValue arg2, ArdenValue arg3) {
			return WITHINTO.runElement(arg1, BinaryOperator.BEFORE.runElement(arg2, arg3), BinaryOperator.AFTER
					.runElement(arg2, arg3));
		};
	};

	public static final TernaryOperator FINDSTRING = new TernaryOperator("FINDSTRING") {
		// FIND <n:string> IN STRING <n:string> STARTING AT <n:number>
		@Override
		public ArdenValue runElement(ArdenValue arg1, ArdenValue arg2, ArdenValue arg3) {
			if (arg1 instanceof ArdenString && arg2 instanceof ArdenString && arg3 instanceof ArdenNumber) {
				String needle = ((ArdenString) arg1).value;
				String haystack = ((ArdenString) arg2).value;
				double start = ((ArdenNumber) arg3).value;
				int startInt = (int) start;
				if (start != startInt)
					return ArdenNull.INSTANCE;
				int result;
				if (startInt >= 1 && startInt <= haystack.length()) {
					result = haystack.indexOf(needle, startInt - 1) + 1;
				} else {
					result = 0;
				}
				return ArdenNumber.create(result, ArdenValue.NOPRIMARYTIME);
			} else {
				return ArdenNull.INSTANCE;
			}
		};
	};

	public static final TernaryOperator SUBSTRING = new TernaryOperator("SUBSTRING") {
		// SUBSTRING <n:number> CHARACTERS [STARTING AT <n:number>] FROM
		// <n:string>
		@Override
		public ArdenValue runElement(ArdenValue arg1, ArdenValue arg2, ArdenValue arg3) {
			int startLocation = 0;
			if (arg2 != null) {
				// if 'STARTING AT' is specified, the value must be a positive
				// integer
				startLocation = RuntimeHelpers.getPrimitiveIntegerValue(arg2);
				if (startLocation < 1)
					return ArdenNull.INSTANCE;
			}
			if (arg1 instanceof ArdenNumber && arg3 instanceof ArdenString) {
				double count = ((ArdenNumber) arg1).value;
				String input = ((ArdenString) arg3).value;

				int countInt = (int) count;
				if (count != countInt)
					return ArdenNull.INSTANCE;
				String result;
				if (startLocation > input.length()) {
					result = "";
				} else {
					if (startLocation == 0)
						startLocation = (countInt < 0) ? input.length() : 1;

					if (countInt < 0) {
						if (countInt < -startLocation)
							result = input.substring(0, startLocation);
						else
							result = input.substring(startLocation + countInt, startLocation);
					} else {
						startLocation--; // to 0-based coordinates
						if (countInt > input.length() - startLocation)
							result = input.substring(startLocation);
						else
							result = input.substring(startLocation, startLocation + countInt);
					}
				}
				return new ArdenString(result, arg3.primaryTime);
			} else {
				return ArdenNull.INSTANCE;
			}
		};
	};

	public TernaryOperator(String name) {
		this.name = name;
	}

	/** Helper method for handling of primary times */
	public static final long combinePrimaryTime(long time1, long time2, long time3) {
		if (time1 == time2 && time1 == time3)
			return time1;
		else
			return ArdenValue.NOPRIMARYTIME;
	}

	/** Implements the list logic for running the operator. */
	public final ArdenValue run(ArdenValue arg1, ArdenValue arg2, ArdenValue arg3) {
		if (arg1 instanceof ArdenList) {
			ArdenValue[] args1 = ((ArdenList) arg1).values;
			return runList(args1, repeat(arg2, args1.length), repeat(arg3, args1.length));
		} else if (arg2 instanceof ArdenList) {
			ArdenValue[] args2 = ((ArdenList) arg2).values;
			return runList(repeat(arg1, args2.length), args2, repeat(arg3, args2.length));
		} else if (arg3 instanceof ArdenList) {
			ArdenValue[] args3 = ((ArdenList) arg3).values;
			return runList(repeat(arg1, args3.length), repeat(arg2, args3.length), args3);
		} else {
			return runElement(arg1, arg2, arg3);
		}
	}

	private ArdenValue runList(ArdenValue[] arg1, ArdenValue[] arg2, ArdenValue[] arg3) {
		if (arg1.length != arg2.length || arg1.length != arg3.length)
			return ArdenNull.INSTANCE;
		ArdenValue[] result = new ArdenValue[arg1.length];
		for (int i = 0; i < result.length; i++)
			result[i] = runElement(arg1[i], arg2[i], arg3[i]);
		return new ArdenList(result);
	}

	private static ArdenValue[] repeat(ArdenValue arg, int times) {
		if (arg instanceof ArdenList)
			return ((ArdenList) arg).values;
		ArdenValue[] arr = new ArdenValue[times];
		for (int i = 0; i < times; i++)
			arr[i] = arg;
		return arr;
	}

	@Override
	public String toString() {
		return name;
	}
}
