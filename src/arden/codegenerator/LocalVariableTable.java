package arden.codegenerator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Speichert Variablennamen für Debugger.
 * 
 * @author daniel
 * 
 */
class LocalVariableTable {
	final int attributeNameIndex;
	private final ConstantPool pool;
	private ArrayList<LocalVariable> variables = new ArrayList<LocalVariable>();
	
	static class LocalVariable
	{
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