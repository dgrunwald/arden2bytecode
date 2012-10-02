package arden.codegenerator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

final class ExceptionTable {
	private final ConstantPool pool;
	private ArrayList<ExceptionRange> ranges = new ArrayList<ExceptionRange>();
	
	public ExceptionTable(ConstantPool pool) {
		this.pool = pool;
	}
	
	public void addExceptionRange(
			Label start, 
			Label end, 
			Label handlerBegin, 
			Class<? extends Throwable> exception) {
		ranges.add(new ExceptionRange(pool.getClass(exception), start, end, handlerBegin));
	}
	
	public byte[] getData() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeShort(ranges.size());
			for (ExceptionRange range : ranges) {
				dos.writeShort(range.start.markedPosition);
				dos.writeShort(range.end.markedPosition);
				dos.writeShort(range.handlerBegin.markedPosition);
				dos.writeShort(range.catchType);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}
	
	static class ExceptionRange {
		int catchType;
		Label start, end, handlerBegin;
		public ExceptionRange(int catchType, Label start, Label end, Label handlerBegin) {
			this.catchType = catchType;
			this.start = start;
			this.end = end;
			this.handlerBegin = handlerBegin;
		}
	}
}
