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

public final class ArdenBoolean extends ArdenValue {
	public static final ArdenBoolean TRUE = new ArdenBoolean(true, NOPRIMARYTIME);
	public static final ArdenBoolean FALSE = new ArdenBoolean(false, NOPRIMARYTIME);

	public final boolean value;

	private ArdenBoolean(boolean value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	public static ArdenBoolean create(boolean value, long primaryTime) {
		if (primaryTime == NOPRIMARYTIME)
			return value ? TRUE : FALSE;
		else
			return new ArdenBoolean(value, primaryTime);
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return create(value, newPrimaryTime);
	}
	
	@Override
	public boolean isTrue() {
		return value;
	}

	@Override
	public boolean isFalse() {
		return !value;
	}

	@Override
	public String toString() {
		return value ? "true" : "false";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArdenBoolean)
			return value == ((ArdenBoolean) obj).value;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return value ? 42 : 23;
	}
}
