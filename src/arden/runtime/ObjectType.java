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

/**
 * Represents the runtime type of an ArdenObject.
 * 
 * @author Daniel Grunwald
 * 
 */
public class ObjectType {
	public final String name;
	public final String[] fieldNames;

	public ObjectType(String name, String[] fieldNames) {
		if (name == null || fieldNames == null)
			throw new NullPointerException();
		this.name = name;
		this.fieldNames = fieldNames;
	}

	public int getFieldIndex(String uppercaseName) {
		for (int i = 0; i < fieldNames.length; i++) {
			if (fieldNames[i].equalsIgnoreCase(uppercaseName))
				return i;
		}
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(name);
		b.append(" := OBJECT [ ");
		for (int i = 0; i < fieldNames.length; i++) {
			if (i > 0)
				b.append(", ");
			b.append(fieldNames[i]);
		}
		b.append(" ]");
		return b.toString();
	}
}
