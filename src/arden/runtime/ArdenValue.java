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

public abstract class ArdenValue {
	public static final long NOPRIMARYTIME = Long.MIN_VALUE;
	public final long primaryTime;

	protected ArdenValue() {
		this.primaryTime = NOPRIMARYTIME;
	}

	protected ArdenValue(long primaryTime) {
		this.primaryTime = primaryTime;
	}

	/**
	 * Creates a copy of this value with the primary time set to newPrimaryTime.
	 */
	public abstract ArdenValue setTime(long newPrimaryTime);

	/**
	 * Returns whether this value is 'true' in a boolean context. Returns 'true'
	 * for boolean true; false for everything else (even for the list ",true")
	 */
	public boolean isTrue() {
		return false;
	}

	/**
	 * Returns whether this value is 'false' in a boolean context. Returns
	 * 'true' for boolean false; returns 'false' for everything else (boolean
	 * true or null)
	 */
	public boolean isFalse() {
		return false;
	}

	/** Gets the elements that a FOR loop will iterate through */
	public ArdenValue[] getElements() {
		return new ArdenValue[] { this };
	}

	/**
	 * Compares this ArdenValue with another. Does not implement Comparable
	 * interface because we have an additional return value MIN_VALUE with
	 * special meaning.
	 * 
	 * @return Returns Integer.MIN_VALUE if the types don't match or the type is
	 *         not ordered. Returns -1 if this is less than rhs. Returns 0 if
	 *         this is equal to rhs. Returns 1 if this is larger than rhs.
	 */
	public int compareTo(ArdenValue rhs) {
		return Integer.MIN_VALUE;
	}
}
