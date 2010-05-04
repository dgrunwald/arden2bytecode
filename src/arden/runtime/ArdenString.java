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

public final class ArdenString extends ArdenValue {
	public final String value;

	public ArdenString(String value) {
		this.value = value;
	}

	public ArdenString(String value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new ArdenString(value, newPrimaryTime);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			b.append(c);
			if (c == '"')
				b.append('"'); // double "
		}
		b.append('"');
		return b.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ArdenString) && value.equals(((ArdenString) obj).value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public int compareTo(ArdenValue rhs) {
		if (rhs instanceof ArdenString) {
			return Integer.signum(value.compareTo(((ArdenString) rhs).value));
		} else {
			return Integer.MIN_VALUE;
		}
	}
}
