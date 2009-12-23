package arden.codegenerator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Stores line numbers for exception stacktrace/debugger.
 * 
 * @author Daniel Grunwald
 * 
 */
class LineNumberTable {
	final int attributeNameIndex;
	private ArrayList<Integer> programCounters = new ArrayList<Integer>();
	private ArrayList<Integer> lineNumbers = new ArrayList<Integer>();

	public LineNumberTable(ConstantPool pool) {
		attributeNameIndex = pool.getUtf8("LineNumberTable");
	}

	public void addEntry(int pc, int lineNumber) {
		programCounters.add(pc);
		lineNumbers.add(lineNumber);
	}

	public byte[] getData() {
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(byteOutputStream);
			data.writeShort(lineNumbers.size());
			for (int i = 0; i < lineNumbers.size(); i++) {
				data.writeShort(programCounters.get(i));
				data.writeShort(lineNumbers.get(i));
			}
			data.flush();
			return byteOutputStream.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
