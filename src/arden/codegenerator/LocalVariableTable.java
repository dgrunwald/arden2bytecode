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

package arden.codegenerator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Stores variable names for debuggers.
 * 
 * @author Daniel Grunwald
 * 
 */
final class LocalVariableTable {
	final int attributeNameIndex;
	private final ConstantPool pool;
	private ArrayList<LocalVariable> variables = new ArrayList<LocalVariable>();

	static class LocalVariable {
		int index, nameIndex, descriptorIndex;

		public LocalVariable(int index, int nameIndex, int descriptorIndex) {
			this.index = index;
			this.nameIndex = nameIndex;
			this.descriptorIndex = descriptorIndex;
		}
	}

	public LocalVariableTable(ConstantPool pool) {
		this.pool = pool;
		attributeNameIndex = pool.getUtf8("LocalVariableTable");
	}

	public void addEntry(int index, String variableName, String variableDescriptor) {
		variables.add(new LocalVariable(index, pool.getUtf8(variableName), pool.getUtf8(variableDescriptor)));
	}

	public byte[] getData(int methodBodyLength) {
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(byteOutputStream);
			data.writeShort(variables.size());
			for (LocalVariable v : variables) {
				data.writeShort(0); // start_pc
				data.writeShort(methodBodyLength);
				data.writeShort(v.nameIndex);
				data.writeShort(v.descriptorIndex);
				data.writeShort(v.index);
			}
			data.flush();
			return byteOutputStream.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}