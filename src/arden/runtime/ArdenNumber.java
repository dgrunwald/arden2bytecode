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

public final class ArdenNumber extends ArdenValue {
	public static final ArdenNumber ZERO = new ArdenNumber(0);
	public static final ArdenNumber ONE_HUNDRED = new ArdenNumber(100);

	public final double value;

	public ArdenNumber(double value) {
		this.value = value;
	}

	private ArdenNumber(double value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	public static ArdenValue create(double value, long primaryTime) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return ArdenNull.create(primaryTime);
		else if (value == 0 && primaryTime == NOPRIMARYTIME)
			return ZERO;
		else
			return new ArdenNumber(value, primaryTime);
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return create(value, newPrimaryTime);
	}

	@Override
	public String toString() {
		return toString(value);
	}

	public static String toString(double num) {
		int i = (int) num;
		if (i == num)
			return Integer.toString(i);
		else
			return Double.toString(num);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ArdenNumber) && (value == ((ArdenNumber) obj).value);
	}

	@Override
	public int hashCode() {
		return new Double(value).hashCode();
	}

	@Override
	public int compareTo(ArdenValue rhs) {
		if (rhs instanceof ArdenNumber) {
			double rval = ((ArdenNumber) rhs).value;
			if (value < rval)
				return -1;
			else if (value > rval)
				return 1;
			else
				return 0;
		}
		return Integer.MIN_VALUE;
	}
}
