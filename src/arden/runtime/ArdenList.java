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

public final class ArdenList extends ArdenValue {
	public final static ArdenList EMPTY = new ArdenList(new ArdenValue[0]);

	public final ArdenValue[] values;

	public ArdenList(ArdenValue[] values) {
		this.values = values;
	}
	
	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		ArdenValue[] newValues = new ArdenValue[values.length];
		for (int i = 0; i < values.length; i++)
			newValues[i] = values[i].setTime(newPrimaryTime);
		return new ArdenList(newValues);
	}
	
	@Override
	public ArdenValue[] getElements() {
		return values;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append('(');
		if (values.length == 1) {
			b.append(',');
			b.append(values[0].toString());
		} else if (values.length > 1) {
			b.append(values[0].toString());
			for (int i = 1; i < values.length; i++) {
				b.append(',');
				b.append(values[i].toString());
			}
		}
		b.append(')');
		return b.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ArdenList))
			return false;
		ArdenList list = (ArdenList) obj;
		if (list.values.length != values.length)
			return false;
		for (int i = 0; i < values.length; i++) {
			if (!values[i].equals(list.values[i]))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 1;
		for (ArdenValue val : values) {
			result *= 27;
			result += val.hashCode();
		}
		return result;
	}
}
