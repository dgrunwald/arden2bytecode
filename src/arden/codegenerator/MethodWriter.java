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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * This class is used for emitting the byte code in the body of methods.
 * 
 * @author Daniel Grunwald
 * 
 */
public final class MethodWriter {
	/** ConstantPool for method- and field references */
	private final ConstantPool pool;

	/** Line number table, used if debugging information should be output */
	private LineNumberTable lineNumberTable;

	/** Exception table declaring jump targets for given Exceptions */
	private ExceptionTable exceptionTable;
	
	/** Table of local variables (for use by debuggers) */
	private LocalVariableTable localVariableTable;

	/** OutputStream: all byte code is written here */
	private final ByteArrayOutputStream byteCodeOutputStream = new ByteArrayOutputStream();

	/** DataOutputStream that wraps the byteCodeOutputStream */
	private final DataOutputStream byteCode = new DataOutputStream(byteCodeOutputStream);

	private final boolean isInstanceMethod;

	/** Stores positions, where the target of labels has to be placed. */
	private final ArrayList<LabelReference> labelReferences = new ArrayList<LabelReference>();

	/** Number of local variables including parameters and 'this' */
	private int numLocals;

	/**
	 * Current stack size.
	 * 
	 * Every instruction being emitted will adjust this value by calling
	 * poppush, so that the current size of the Java stack is known. After
	 * unconditional jump instructions the stack size is unknown, this is
	 * represented using the value -1. In this case, the stack size must be
	 * inferred from a jump mark (by calling mark()) before additional
	 * instructions can be emitted.
	 */
	private int stackSize;

	/** Maximum stack size that was observed so far */
	private int maxStackSize;

	/**
	 * MethodWriter constructor.
	 */
	public MethodWriter(ConstantPool pool, boolean isInstanceMethod, int parameterCount) {
		if (pool == null)
			throw new NullPointerException();
		this.pool = pool;
		this.isInstanceMethod = isInstanceMethod;
		this.numLocals = parameterCount + (isInstanceMethod ? 1 : 0);
	}

	/** Adjusts stackSize for one operation (to calculate maxStackSize) */
	private void poppush(int popsize, int pushsize) {
		if (stackSize == -1) {
			// Code is unreachable.
			return;
		}

		stackSize -= popsize;
		if (stackSize < 0)
			throw new IllegalStateException("Stack underflow.");
		stackSize += pushsize;
		if (stackSize > maxStackSize) {
			maxStackSize = stackSize;
			if (stackSize >= 65534)
				throw new ClassFileLimitExceededException("Stack overflow.");
		}
	}

	/** Emits one byte (0-255) into the byte code. */
	private void emit(int b) {
		if (b < 0 || b > 255)
			throw new IllegalArgumentException("Number out of range");
		if (stackSize == -1)
			return; // don't emit unreachable code
		try {
			byteCode.writeByte(b);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Emits a signed byte (-128 to 127) into the byte code. */
	private void emitInt8(int b) {
		if (b < Byte.MIN_VALUE || b > Byte.MAX_VALUE)
			throw new IllegalArgumentException("Number out of range");
		if (stackSize == -1)
			return; // don't emit unreachable code
		try {
			byteCode.writeByte(b);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Emits an unsigned short into the byte code. */
	private void emitUInt16(int num) {
		if (num < 0 || num > 65535)
			throw new IllegalArgumentException("Number out of range");
		if (stackSize == -1)
			return; // don't emit unreachable code
		try {
			byteCode.writeShort(num);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Emits a signed short into the byte code. */
	private void emitInt16(int num) {
		if (num < -32768 || num > 32767)
			throw new IllegalArgumentException("Number out of range");
		if (stackSize == -1)
			return; // don't emit unreachable code
		try {
			byteCode.writeShort(num);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Emits an int into the byte code. */
	private void emitInt32(int num) {
		if (stackSize == -1)
			return; // don't emit unreachable code
		try {
			byteCode.writeInt(num);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Returns the current position in the byte code */
	private int getCurrentPosition() {
		return byteCode.size();
	}

	/** Emits the target location of a label (16-bit offset) */
	private void emitLabelReference(Label label, int basePosition) {
		if (checkLabel(label)) {
			int referencePosition = getCurrentPosition();
			// emit place holder
			emitUInt16(0);
			// remember position, to fill in the actual target later
			labelReferences.add(new LabelReference(referencePosition, basePosition, label, false));
		}
	}

	/** Emits the target location of a label (32-bit offset) */
	private void emitLabelReference32(Label label, int basePosition) {
		if (checkLabel(label)) {
			int referencePosition = getCurrentPosition();
			// emit place holder
			emitInt32(0);
			// remember position, to fill in the actual target later
			labelReferences.add(new LabelReference(referencePosition, basePosition, label, true));
		}
	}

	private boolean checkLabel(Label label) {
		if (label == null)
			throw new IllegalArgumentException();
		if (stackSize == -1)
			return false; // don't emit unreachable code
		if (!label.allowJumps)
			throw new IllegalArgumentException("Cannot emit backward jump to forward-only label.");
		if (label.stackSize == -1)
			label.stackSize = stackSize;
		else if (label.stackSize != stackSize)
			throw new IllegalArgumentException("All paths reaching a label must result in the same stack size.");
		return true;
	}

	/**
	 * Fills in the target addresses of all labels into the place holders.
	 */
	private void resolveLabels(byte[] byteCode) {
		for (LabelReference labelRef : labelReferences) {
			if (labelRef.label.markedPosition < 0)
				throw new IllegalStateException("Cannot resolve label - did you use a label without calling mark()?");
			int offset = labelRef.label.markedPosition - labelRef.basePosition;
			if (labelRef.is32BitOffset) {
				byteCode[labelRef.referencePosition] = (byte) (offset >>> 24);
				byteCode[labelRef.referencePosition + 1] = (byte) ((offset >> 16) & 0xff);
				byteCode[labelRef.referencePosition + 2] = (byte) ((offset >> 8) & 0xff);
				byteCode[labelRef.referencePosition + 3] = (byte) (offset & 0xff);
			} else {
				if (offset < Short.MIN_VALUE || offset > Short.MAX_VALUE)
					throw new ClassFileLimitExceededException("Branch distance too large.");
				byteCode[labelRef.referencePosition] = (byte) ((short) offset >>> 8);
				byteCode[labelRef.referencePosition + 1] = (byte) (offset & 0xff);
			}
		}
		labelReferences.clear();
	}

	/** Returns the emitted byte code. */
	public byte[] getByteCode() {
		try {
			byteCode.flush();
			byte[] code = byteCodeOutputStream.toByteArray();
			if (code.length > 65534)
				throw new ClassFileLimitExceededException("Too much code.");
			resolveLabels(code);
			return code;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns the content of the "Code" attribute. This contains the byte code
	 * and some additional data (maxStackSize etc.)
	 */
	public byte[] getCodeAttributeData() {
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(byteOutputStream);
			data.writeShort(maxStackSize);
			data.writeShort(numLocals);
			byte[] code = getByteCode();
			data.writeInt(code.length);
			data.write(code);
			if (exceptionTable == null) {
				data.writeShort(0); // exception_table_length
			} else {
				data.write(exceptionTable.getData());
			}
			int attributesCount = 0;
			if (lineNumberTable != null)
				attributesCount++;
			if (localVariableTable != null)
				attributesCount++;
			data.writeShort(attributesCount); // attributes_count
			if (lineNumberTable != null) {
				data.writeShort(lineNumberTable.attributeNameIndex);
				byte[] table = lineNumberTable.getData();
				data.writeInt(table.length);
				data.write(table);
			}
			if (localVariableTable != null) {
				data.writeShort(localVariableTable.attributeNameIndex);
				byte[] table = localVariableTable.getData(code.length);
				data.writeInt(table.length);
				data.write(table);
			}
			data.flush();
			return byteOutputStream.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Enables the LineNumberTable, which stores sequence points for debuggers.
	 */
	public void enableLineNumberTable() {
		if (lineNumberTable != null)
			throw new IllegalStateException("Line number table is already enabled.");
		lineNumberTable = new LineNumberTable(pool);
		localVariableTable = new LocalVariableTable(pool);
	}

	/**
	 * Defines a new local variable. This is not necessary for using variables;
	 * calling defineLocalVariable just provides additional information about
	 * the variable for debuggers.
	 */
	public void defineLocalVariable(int vindex, String name, Class<?> type) {
		if (localVariableTable != null)
			localVariableTable.addEntry(vindex, name, ConstantPool.createFieldDescriptor(type));
	}

	/**
	 * Marks the beginning of a statement. Used by debuggers and for display of
	 * line numbers in stack traces.
	 */
	public void sequencePoint(int lineNumber) {
		if (stackSize > 0)
			throw new IllegalStateException("Expecting empty stack at start of statement.");
		if (lineNumberTable != null)
			lineNumberTable.addEntry(byteCode.size(), lineNumber);
	}

	/**
	 * Marks a try..catch region.
	 * @param start Beginning of region where thrown Exceptions should be handled
	 * @param end End of region where thrown Exceptions should be handled
	 * @param handler Start of Exception handling code
	 * @param exceptionType Type of Exception to be thrown
	 */
	public void addExceptionInfo(Label start, Label end, Label handler, Class<? extends Throwable> exceptionType) {
		if (exceptionTable == null) {
			exceptionTable = new ExceptionTable(pool);
		}
		exceptionTable.addExceptionRange(start, end, handler, exceptionType);
	}
	
	private void emitLdc(int constantIndex) {
		if (constantIndex < 256) {
			emit(18); // ldc
			emit(constantIndex);
		} else {
			emit(19); // ldc_w
			emitUInt16(constantIndex);
		}
	}

	/**
	 * Loads a constant int onto the stack.
	 * 
	 * Stack: .. => .., val
	 */
	public void loadIntegerConstant(int val) {
		poppush(0, 1);
		if (val >= -1 && val <= 5) {
			emit(val + 3); // iconst_<val>
		} else if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE) {
			emit(16); // bipush
			emitInt8(val);
		} else {
			emitLdc(pool.getInteger(val));
		}
	}

	/**
	 * Loads a constant string onto the stack.
	 * 
	 * Stack: .. => .., val
	 */
	public void loadStringConstant(String val) {
		if (val == null) {
			loadNull();
		} else {
			poppush(0, 1);
			emitLdc(pool.getString(val));
		}
	}

	/**
	 * Loads a constant double onto the stack.
	 * 
	 * Stack: .. => .., val
	 */
	public void loadDoubleConstant(double val) {
		poppush(0, 2);
		if (val == 0) {
			emit(14); // dconst_0
		} else if (val == 1) {
			emit(15); // dconst_1
		} else {
			emit(20); // ldc2_w
			emitUInt16(pool.getDouble(val));
		}
	}

	/**
	 * Loads a constant long onto the stack.
	 * 
	 * Stack: .. => .., val
	 */
	public void loadLongConstant(long val) {
		poppush(0, 2);
		if (val == 0) {
			emit(9); // lconst_0
		} else if (val == 1) {
			emit(10); // lconst_1
		} else {
			emit(20); // ldc2_w
			emitUInt16(pool.getLong(val));
		}
	}

	/**
	 * Loads the null reference onto the stack.
	 * 
	 * Stack: .. => .., null
	 */
	public void loadNull() {
		poppush(0, 1);
		emit(1); // aconst_null
	}

	/**
	 * Loads the this reference onto the stack.
	 * 
	 * Stack: .. => .., this
	 */
	public void loadThis() {
		if (!isInstanceMethod)
			throw new IllegalArgumentException("Cannot load 'this' in static method.");
		loadVariable(0);
	}

	private void checkLocalCount(int vindex) {
		if (vindex < 0)
			throw new IllegalArgumentException("vindex must be positive");
		if (vindex >= numLocals) {
			numLocals = vindex + 1;
			if (numLocals > 65535)
				throw new ClassFileLimitExceededException("Too many locals.");
		}
	}

	/**
	 * Loads the value of a reference-type variable onto the stack.
	 * 
	 * Stack: .. => .., reference
	 */
	public void loadVariable(int vindex) {
		checkLocalCount(vindex);
		poppush(0, 1);
		if (vindex < 4) {
			emit(42 + vindex); // aload_vindex
		} else if (vindex <= 255) {
			emit(25); // aload
			emit(vindex);
		} else {
			emit(196); // wide
			emit(25); // aload
			emitUInt16(vindex);
		}
	}

	/**
	 * Loads the value of a primitive int variable onto the stack.
	 * 
	 * Stack: .. => .., value
	 */
	public void loadIntVariable(int vindex) {
		checkLocalCount(vindex);
		poppush(0, 1);
		if (vindex < 4) {
			emit(26 + vindex); // iload_vindex
		} else if (vindex <= 255) {
			emit(21); // iload
			emit(vindex);
		} else {
			emit(196); // wide
			emit(21); // iload
			emitUInt16(vindex);
		}
	}

	/**
	 * Stores a reference from the stack in the local variable.
	 * 
	 * Stack: .., reference => ..
	 */
	public void storeVariable(int vindex) {
		checkLocalCount(vindex);
		poppush(1, 0);
		if (vindex < 4) {
			emit(75 + vindex); // astore_vindex
		} else if (vindex <= 255) {
			emit(58); // astore
			emit(vindex);
		} else {
			emit(196); // wide
			emit(58); // astore
			emitUInt16(vindex);
		}
	}

	/**
	 * Stores an int value from the stack in the local variable.
	 * 
	 * Stack: .., value => ..
	 */
	public void storeIntVariable(int vindex) {
		checkLocalCount(vindex);
		poppush(1, 0);
		if (vindex < 4) {
			emit(59 + vindex); // istore_vindex
		} else if (vindex <= 255) {
			emit(54); // istore
			emit(vindex);
		} else {
			emit(196); // wide
			emit(54); // istore
			emitUInt16(vindex);
		}
	}

	/**
	 * Increments a primitive int-variable by a constant amount.
	 * 
	 * Stack: .. => ..
	 */
	public void incVariable(int vindex, int incrementAmount) {
		checkLocalCount(vindex);
		poppush(0, 0);

		if (vindex <= 255 && (incrementAmount >= -128 && incrementAmount < 128)) {
			emit(132); // iinc
			emitInt8(vindex);
			emitInt8(incrementAmount);
		} else {
			emit(196); // wide
			emit(132); // iinc
			emitUInt16(vindex);
			emitInt16(incrementAmount);
		}
	}

	/**
	 * Generates the 'nop'-instruction.
	 */
	public void nop() {
		emit(0); // nop
	}

	/**
	 * Removes the top value from the stack.
	 * 
	 * Stack: .., x => ..
	 */
	public void pop() {
		poppush(1, 0);
		emit(87); // pop
	}

	/**
	 * Removes the top two values from the stack.
	 * 
	 * Stack: .., x, y => ..
	 */
	public void pop2() {
		poppush(2, 0);
		emit(88); // pop2
	}

	/**
	 * Duplicates the top value on the stack.
	 * 
	 * Stack: .., x => .., x, x
	 */
	public void dup() {
		poppush(1, 2);
		emit(89); // dup
	}

	/**
	 * Duplicates the top two values on the stack.
	 * 
	 * Stack: .., y, x => .., y, x, y, x
	 */
	public void dup2() {
		poppush(2, 4);
		emit(92); // dup2
	}

	/**
	 * Duplicates the top value on the stack and inserts it at the 3rd position.
	 * 
	 * Stack: .., y, x => .., x, y, x
	 */
	public void dup_x1() {
		poppush(2, 3);
		emit(90); // dup_x1
	}

	/**
	 * Duplicates the top two-word item on the stack and inserts the duplicate
	 * before the previous (single-word) item on the stack. Alternatively, this
	 * instruction could also be used to duplicate two single-word items and
	 * insert them before the third single-word item on the stack.
	 * 
	 * Stack: .., z, y, x => .., x, y, z, y, x
	 */
	public void dup2_x1() {
		poppush(3, 5);
		emit(93); // dup2_x1
	}

	/**
	 * Duplicates the top value on the stack and inserts it at the 4th position.
	 * 
	 * Stack: .., z, y, x => .., x, z, y, x
	 */
	public void dup_x2() {
		poppush(3, 4);
		emit(91); // dup_x2
	}

	/**
	 * Swaps the top two elements on the stack
	 * 
	 * Stack: .., x, y => .., y, x
	 */
	public void swap() {
		poppush(2, 2);
		emit(95); // swap
	}

	private void emitJump(int opcode, Label label) {
		int basePosition = getCurrentPosition();
		emit(opcode);
		emitLabelReference(label, basePosition);
	}

	private void unconditionalControlTransfer() {
		// following an unconditional jump, the new stack size is unknown:
		// it doesn't depend on the previous stack size, but only on jumps
		// pointing to the new instruction
		stackSize = -1;
	}

	/** Unconditional jump to Label. */
	public void jump(Label label) {
		emitJump(167, label); // goto
		unconditionalControlTransfer();
	}

	/**
	 * Jump to Label, if value is 0.
	 * 
	 * Stack: .., int => ..
	 */
	public void jumpIfZero(Label label) {
		poppush(1, 0);
		emitJump(153, label); // ifeq
	}

	/**
	 * Jump to Label, if value is not 0.
	 * 
	 * Stack: .., int => ..
	 */
	public void jumpIfNonZero(Label label) {
		poppush(1, 0);
		emitJump(154, label); // ifne
	}

	/**
	 * Jump to Label, if value is less than 0.
	 * 
	 * Stack: .., int => ..
	 */
	public void jumpIfNegative(Label label) {
		poppush(1, 0);
		emitJump(155, label); // iflt
	}

	/**
	 * Jump to Label, if lhs is less than rhs.
	 * 
	 * Stack: .., lhs (int), rhs (int) => ..
	 */
	public void jumpIfLessThan(Label label) {
		poppush(2, 0);
		emitJump(161, label); // if_icmplt
	}
	
	/**
	 * Jump to Label, if lhs is less than or equal to rhs.
	 * 
	 * Stack: .., lhs (int), rhs (int) => ..
	 */
	public void jumpIfLessThanOrEqual(Label label) {
		poppush(2, 0);
		emitJump(160, label); // if_icmple
	}

	/**
	 * Jump to Label, if reference is null.
	 * 
	 * Stack: .., objectref => ..
	 */
	public void jumpIfNull(Label label) {
		poppush(1, 0);
		emitJump(198, label); // ifnull
	}

	/**
	 * Jump to Label, if reference is not null.
	 * 
	 * Stack: .., objectref => ..
	 */
	public void jumpIfNonNull(Label label) {
		poppush(1, 0);
		emitJump(199, label); // ifnonnull
	}

	/**
	 * Jump to Label, if references point to the same object.
	 * 
	 * Stack: .., obj1, obj2 => ..
	 */
	public void jumpIfReferenceEqual(Label label) {
		poppush(2, 0);
		emitJump(165, label); // if_acmpeg
	}

	/**
	 * Jump to Label, if references point to different objects.
	 * 
	 * Stack: .., obj1, obj2 => ..
	 */
	public void jumpIfReferenceNotEqual(Label label) {
		poppush(2, 0);
		emitJump(166, label); // if_acmpne
	}

	/**
	 * Emits a switch instruction. A primitive int value from the top of the
	 * will be compared with a set of values.
	 * 
	 * @param constantValues
	 *            The values for comparing with. The array must be sorted.
	 * @param targetLabels
	 *            The labels for jumping to. if (intvalue == constantValues[i])
	 *            goto targetLabels[i];
	 * @param defaultLabel
	 *            The default label, used if the int value doesn't match any of
	 *            the constantValues.
	 * 
	 *            Stack: .., int => ..
	 */
	public void lookupSwitch(int[] constantValues, Label[] targetLabels, Label defaultLabel) {
		if (constantValues.length != targetLabels.length)
			throw new IllegalArgumentException("Values and label arrays must have same length");
		if (constantValues.length == 0)
			throw new IllegalArgumentException("Empty switch not supported");
		for (int i = 0; i < constantValues.length; i++) {
			if (i > 0 && constantValues[i - 1] >= constantValues[i])
				throw new IllegalArgumentException("Constants are not sorted/duplicate constant");
		}
		poppush(1, 0);
		int basePosition = getCurrentPosition();
		if (constantValues[0] + constantValues.length - 1 == constantValues[constantValues.length - 1]) {
			emit(170); // tableswitch
			while (getCurrentPosition() % 4 != 0)
				emit(0); // pad to multiple of 4
			emitLabelReference32(defaultLabel, basePosition);
			emitInt32(constantValues[0]); // low
			emitInt32(constantValues[constantValues.length - 1]); // high
			for (int i = 0; i < constantValues.length; i++) {
				emitLabelReference32(targetLabels[i], basePosition);
			}
		} else {
			emit(171); // lookupswitch
			while (getCurrentPosition() % 4 != 0)
				emit(0); // pad to multiple of 4
			emitLabelReference32(defaultLabel, basePosition);
			emitInt32(constantValues.length);
			for (int i = 0; i < constantValues.length; i++) {
				emitInt32(constantValues[i]);
				emitLabelReference32(targetLabels[i], basePosition);
			}
		}
		unconditionalControlTransfer();
	}

	/**
	 * Marks the current code location with the specified Label. Only forward
	 * jumps to the label are allowed. The stack does not have to be empty for
	 * using this method.
	 */
	public void markForwardJumpsOnly(Label label) {
		if (label.markedPosition == -1) {
			label.markedPosition = getCurrentPosition();
			label.allowJumps = false;
			// "synchronize" stackSize with label.stackSize
			if (stackSize == -1) {
				stackSize = label.stackSize;
			} else if (label.stackSize == -1) {
				label.stackSize = stackSize;
			} else {
				if (stackSize != label.stackSize)
					throw new IllegalArgumentException(
							"Label cannot be placed here: A jump to this position expects stack size" + label.stackSize
									+ ", but stack size is " + stackSize);
			}
		} else {
			throw new IllegalArgumentException("The label already was used to mark a position.");
		}
	}

	/**
	 * Marks the current code location with the specified Label. Both forward
	 * and backward jumps to the label are allowed. The stack must be empty
	 * during jumps.
	 */
	public void mark(Label label) {
		if (label.markedPosition == -1) {
			label.markedPosition = getCurrentPosition();
			if (stackSize > 0)
				throw new IllegalArgumentException(
						"Label cannot be placed here: Stack must be empty at label position, but has size " + stackSize);
			if (label.stackSize > 0)
				throw new IllegalArgumentException(
						"Label cannot be placed here: Stack must be empty during jump, but there is a jump source with stack size "
								+ label.stackSize);
			stackSize = 0;
			label.stackSize = 0;
		} else {
			throw new IllegalArgumentException("The label already was used to mark a position.");
		}
	}
	
	public void markExceptionHandler(Label label) {
		if (label.markedPosition == -1) {
			label.markedPosition = getCurrentPosition();
			stackSize = -1;
			label.stackSize = -1;
		} else {
			throw new IllegalArgumentException("The label already was used to mark a position.");
		}
	}

	/** Emits the 'return' instruction. */
	public void returnFromProcedure() {
		emit(177); // return
		unconditionalControlTransfer();
	}

	/**
	 * Emits the 'ireturn' instruction.
	 * 
	 * Stack: .., returnvalue
	 */
	public void returnIntFromFunction() {
		poppush(1, 0);
		emit(172); // ireturn
		unconditionalControlTransfer();
	}

	/**
	 * Emits the 'areturn' instruction.
	 * 
	 * Stack: .., returnvalue
	 */
	public void returnObjectFromFunction() {
		poppush(1, 0);
		emit(176); // areturn
		unconditionalControlTransfer();
	}

	/**
	 * Emits the 'dreturn' instruction.
	 * 
	 * Stack: .., returnvalue
	 */
	public void returnDoubleFromFunction() {
		poppush(2, 0);
		emit(175); // dreturn
		unconditionalControlTransfer();
	}

	/**
	 * Loads the value from a reference-type field.
	 * 
	 * Stack: .., objectref => .., valueref
	 */
	public void loadInstanceField(Field field) {
		if (isStatic(field))
			throw new IllegalArgumentException("Expected instance field, but found static field.");
		loadInstanceField(pool.getFieldref(field));
	}

	/**
	 * Loads the value from a reference-type field.
	 * 
	 * Stack: .., objectref => .., valueref
	 */
	public void loadInstanceField(FieldReference field) {
		poppush(1, 1);
		emit(180); // getfield
		emitUInt16(field.index);
	}

	/**
	 * Saves the reference from the stack in the instance field.
	 * 
	 * Stack: .., objectref, valueref => ..
	 */
	public void storeInstanceField(Field field) {
		if (isStatic(field))
			throw new IllegalArgumentException("Expected instance field, but found static field.");
		storeInstanceField(pool.getFieldref(field));
	}

	/**
	 * Saves the reference from the stack in the instance field.
	 * 
	 * Stack: .., objectref, valueref => ..
	 */
	public void storeInstanceField(FieldReference field) {
		poppush(2, 0);
		emit(181); // putfield
		emitUInt16(field.index);
	}

	/**
	 * Loads the value from a reference-type static field.
	 * 
	 * Stack: .. => .., valueref
	 */
	public void loadStaticField(Field field) {
		if (!isStatic(field))
			throw new IllegalArgumentException("Expected static field, but found instance field.");
		loadStaticField(pool.getFieldref(field));
	}

	/**
	 * Loads the value from a reference-type static field.
	 * 
	 * Stack: .. => .., valueref
	 */
	public void loadStaticField(FieldReference field) {
		poppush(0, 1);
		emit(178); // getstatic
		emitUInt16(field.index);
	}

	/**
	 * Stores a reference from the stack in a static field.
	 * 
	 * Stack: .., valueref => ..
	 */
	public void storeStaticField(Field field) {
		if (!isStatic(field))
			throw new IllegalArgumentException("Expected static field, but found instance field.");
		storeStaticField(pool.getFieldref(field));
	}

	/**
	 * Stores a reference from the stack in a static field.
	 * 
	 * Stack: .., valueref => ..
	 */
	public void storeStaticField(FieldReference field) {
		poppush(1, 0);
		emit(179); // putstatic
		emitUInt16(field.index);
	}

	private static int getStackSize(Class<?> type) {
		if (type.equals(Void.TYPE))
			return 0;
		if (type.equals(Double.TYPE) || type.equals(Long.TYPE))
			return 2;
		else
			return 1;
	}

	private static int getStackSize(Class<?>[] types) {
		int total = 0;
		for (Class<?> type : types)
			total += getStackSize(type);
		return total;
	}

	/**
	 * Calls an instance method.
	 * 
	 * Stack: .., objectref[, parameter1, parameter2] => ..[, returnval]
	 */
	public void invokeInstance(Method method) {
		if (isStatic(method))
			throw new IllegalArgumentException("Cannot use invokeInstance for static method");
		poppush(1 + getStackSize(method.getParameterTypes()), getStackSize(method.getReturnType()));
		emit(182); // invokevirtual
		emitUInt16(pool.getMethodref(method));
	}

	/**
	 * Calls a static method.
	 * 
	 * Stack: ..[, parameter1, parameter2] => ..[, returnval]
	 */
	public void invokeStatic(Method method) {
		if (!isStatic(method))
			throw new IllegalArgumentException("Cannot use invokeStatic for instance method");
		poppush(getStackSize(method.getParameterTypes()), getStackSize(method.getReturnType()));
		emit(184); // invokestatic
		emitUInt16(pool.getMethodref(method));
	}

	private boolean isStatic(Member member) {
		return (member.getModifiers() & Modifier.STATIC) == Modifier.STATIC;
	}

	/**
	 * Creates a new object without calling any constructor.
	 * 
	 * Stack: .. => .., objectref
	 */
	public void newObject(Class<?> type) {
		poppush(0, 1);
		emit(187); // new
		emitUInt16(pool.getClass(type));
	}

	/**
	 * Calls the constructor of an object.
	 * 
	 * Stack: .., objectref, parameters => ..
	 */
	public void invokeConstructor(Constructor<?> ctor) {
		poppush(1 + getStackSize(ctor.getParameterTypes()), 0);
		emit(183); // invokespecial
		emitUInt16(pool.getConstructor(ctor));
	}

	/**
	 * Creates a new array.
	 * 
	 * Stack: .., size => .., arrayref
	 */
	public void newArray(Class<?> elementType) {
		poppush(1, 1);
		emit(189); // anewarray
		emitUInt16(pool.getClass(elementType));
	}

	/**
	 * Stores an object in an array.
	 * 
	 * Stack: .., arrayref, index, value => ..
	 */
	public void storeObjectToArray() {
		poppush(3, 0);
		emit(83); // aastore
	}

	/**
	 * Loads an object from an array.
	 * 
	 * Stack: .., arrayref, index => .., value
	 */
	public void loadObjectFromArray() {
		poppush(2, 1);
		emit(50); // aaload
	}

	/**
	 * Gets the length of an array.
	 * 
	 * Stack: .., arrayref => .., length
	 */
	public void arrayLength() {
		poppush(1, 1);
		emit(190); // arraylength
	}

	/**
	 * Casts an object to the target type. Causes an exception if the cast
	 * fails.
	 * 
	 * Stack: obj => obj
	 */
	public void checkCast(Class<?> targetType) {
		poppush(1, 1);
		emit(192); // checkcast
		emitUInt16(pool.getClass(targetType));
	}

	/**
	 * Casts an object to the target type. Produces null if the cast fails.
	 * 
	 * Stack: obj => obj
	 */
	public void instanceOf(Class<?> targetType) {
		poppush(1, 1);
		emit(193); // instanceof
		emitUInt16(pool.getClass(targetType));
	}
}
