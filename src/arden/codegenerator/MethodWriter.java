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

public class MethodWriter {
	/** ConstantPool für Methoden- und Feldreferenzen */
	private ConstantPool pool;

	/** Line number table, falls Positionsinformationen ausgegeben werden sollen */
	private LineNumberTable lineNumberTable;

	/** Tabelle von lokalen Variablen, für Debugger */
	private LocalVariableTable localVariableTable;

	/** OutputStream, in den der Bytecode geschrieben wird */
	private ByteArrayOutputStream byteCodeOutputStream = new ByteArrayOutputStream();
	/** DataOutputStream zum Schreiben des Bytecodes in den byteCodeOutputStream */
	private DataOutputStream byteCode = new DataOutputStream(
			byteCodeOutputStream);

	private boolean isInstanceMethod;
	
	/**
	 * Speichert Positionen, an denen das Ziel von Labels eingesetzt werden muss
	 */
	private ArrayList<LabelReference> labelReferences = new ArrayList<LabelReference>();

	/** Anzahl lokaler Variablen inklusiv Parameter und 'this' */
	private int numLocals;
	/**
	 * Aktuelle Stackgröße.
	 * 
	 * Jede erzeugte Anweisung passt diesen Wert durch einen Aufruf von poppush
	 * an, so dass immer die aktuelle Größe des Java-Stacks bekannt ist. Nach
	 * unbedingten Sprunganweisungen ist die Stackgröße unbekannt (durch -1
	 * dargestellt). Dann muss zunächst die Stackgröße von einer Sprungmarke
	 * übernommen werden (durch einen mark()-Aufruf), bevor wieder Code
	 * generiert werden kann.
	 */
	private int stackSize;
	/** Maximale Stackgröße */
	private int maxStackSize;

	/**
	 * MethodWriter constructor. 
	 */
	public MethodWriter(ConstantPool pool, boolean isInstanceMethod) {
		this.pool = pool;
		this.isInstanceMethod = isInstanceMethod;
		this.numLocals = isInstanceMethod ? 1 : 0;
	}

	/** Passt stackSize für eine Operation an (um maxStackSize zu ermitteln) */
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

	/** Gibt ein Byte (0-255) in den Bytecode aus. */
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

	/** Gibt ein signed Byte (-128 bis 127) in den Bytecode aus. */
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

	/** Gibt einen unsigned short in den Bytecode aus. */
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

	/** Gibt ein signed short in den Bytecode aus. */
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

	private void emitInt32(int num) {
		if (stackSize == -1)
			return; // don't emit unreachable code
		try {
			byteCode.writeInt(num);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Gibt die aktuelle Position im Bytecode zurück */
	private int getCurrentPosition() {
		return byteCode.size();
	}

	/** Gibt die Zieladdresse eines Labels aus. */
	private void emitLabelReference(Label label, int basePosition) {
		if (checkLabel(label)) {
			int referencePosition = getCurrentPosition();
			// erstmal nur Platzhalter ausgeben
			emitUInt16(0);
			// Position merken, um Label-Ziel später einzutragen
			labelReferences.add(new LabelReference(referencePosition,
					basePosition, label, false));
		}
	}

	/** Gibt die Zieladdresse eines Labels aus. */
	private void emitLabelReference32(Label label, int basePosition) {
		if (checkLabel(label)) {
			int referencePosition = getCurrentPosition();
			// erstmal nur Platzhalter ausgeben
			emitInt32(0);
			// Position merken, um Label-Ziel später einzutragen
			labelReferences.add(new LabelReference(referencePosition,
					basePosition, label, true));
		}
	}

	private boolean checkLabel(Label label) {
		if (label == null)
			throw new IllegalArgumentException();
		if (stackSize == -1)
			return false; // don't emit unreachable code
		if (!label.allowJumps)
			throw new IllegalArgumentException(
					"Cannot emit backward jump to forward-only label.");
		if (label.stackSize == -1)
			label.stackSize = stackSize;
		else if (label.stackSize != stackSize)
			throw new IllegalArgumentException(
					"All paths reaching a label must result in the same stack size.");
		return true;
	}

	/**
	 * Trägt die Zieladdressen von Labels in die dafür vorgesehenen Platzhalter
	 * ein.
	 */
	private void resolveLabels(byte[] byteCode) {
		for (LabelReference labelRef : labelReferences) {
			if (labelRef.label.markedPosition < 0)
				throw new IllegalStateException(
						"Cannot resolve label - did you use a label without calling mark()?");
			int offset = labelRef.label.markedPosition - labelRef.basePosition;
			if (labelRef.is32BitOffset) {
				byteCode[labelRef.referencePosition] = (byte) (offset >>> 24);
				byteCode[labelRef.referencePosition + 1] = (byte) ((offset >> 16) & 0xff);
				byteCode[labelRef.referencePosition + 2] = (byte) ((offset >> 8) & 0xff);
				byteCode[labelRef.referencePosition + 3] = (byte) (offset & 0xff);
			} else {
				if (offset < Short.MIN_VALUE || offset > Short.MAX_VALUE)
					throw new ClassFileLimitExceededException(
							"Branch distance too large.");
				byteCode[labelRef.referencePosition] = (byte) ((short) offset >>> 8);
				byteCode[labelRef.referencePosition + 1] = (byte) (offset & 0xff);
			}
		}
		labelReferences.clear();
	}

	/** Gibt den bisher erzeugten Bytecode zurück. */
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
	 * Gibt den Inhalt des "Code" Attributes zurück. Dieses enthält den Bytecode
	 * sowie einige Zusatzdaten (maxStackSize etc.)
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
			data.writeShort(0); // exception_table_length
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
	 * Aktiviert die Zeilennummertabelle, in der Sequenzpunkte für Debugger
	 * gespeichert werden.
	 */
	public void enableLineNumberTable() {
		if (lineNumberTable != null)
			throw new IllegalStateException(
					"Line number table is already enabled.");
		lineNumberTable = new LineNumberTable(pool);
	}

	/**
	 * Definiert eine lokale Variable. Dies ist nicht zwingend notwendig, ein
	 * Aufruf von legt lediglich einen Eintrag in der LocalVariableTable an, die
	 * von Debuggern verwendet wird.
	 */
	public void defineLocalVariable(int vindex, String name, Class<?> type) {
		if (localVariableTable == null)
			localVariableTable = new LocalVariableTable(pool);
		localVariableTable.addEntry(vindex, name, ConstantPool.createFieldDescriptor(type));
	}

	/**
	 * Markiert den Anfang eines Statements. Die Java Runtime nutzt diese, um
	 * Zeilennummern in Stack Traces anzuzeigen.
	 */
	public void sequencePoint(int lineNumber) {
		if (stackSize > 0)
			throw new IllegalStateException(
					"Expecting empty stack at start of statement.");
		if (lineNumberTable != null)
			lineNumberTable.addEntry(byteCode.size(), lineNumber);
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
	 * Legt einen konstanten Integer auf den Stack (ohne Wrapper-Klasse).
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
	 * Legt einen konstanten String auf den Stack (ohne Wrapper-Klasse).
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
	 * Legt einen konstanten Double auf den Stack (ohne Wrapper-Klasse).
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
	 * Legt einen konstanten Long auf den Stack (ohne Wrapper-Klasse).
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
	 * Legt die null-Referenz auf den Stack.
	 * 
	 * Stack: .. => .., null
	 */
	public void loadNull() {
		poppush(0, 1);
		emit(1); // aconst_null
	}

	/**
	 * Legt die this-Referenz auf den Stack.
	 * 
	 * Stack: .. => .., this
	 */
	public void loadThis() {
		if (isInstanceMethod)
			throw new IllegalArgumentException(
					"Cannot load 'this' in static method.");
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
	 * Legt den Wert einer Variablen auf den Stack.
	 * 
	 * Stack: .. => .., wert
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
	 * Legt den Wert einer primitven int-Variablen auf den Stack.
	 * 
	 * Stack: .. => .., wert
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
	 * Speichert einen Wert vom Stack in der angegebenen Variablen.
	 * 
	 * Stack: .., wert => ..
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
	 * Speichert einen primitiven int-Wert vom Stack in der angegebenen Variablen.
	 * 
	 * Stack: .., wert => ..
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
	 * Erhoeht eine int-Variable um einen konstanten Inkrement
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
	 * Generiert die 'nop'-Instruktion.
	 */
	public void nop() {
		emit(0); // nop
	}

	/**
	 * Entfernt den obersten Wert vom Stack.
	 * 
	 * Stack: .., x => ..
	 */
	public void pop() {
		poppush(1, 0);
		emit(87); // pop
	}

	/**
	 * Dupliziert den obersten Wert auf dem Stack.
	 * 
	 * Stack: .., x => .., x, x
	 */
	public void dup() {
		poppush(1, 2);
		emit(89); // dup
	}

	/**
	 * Dupliziert den obersten Wert auf dem Stack und fügt ihn an dritter
	 * Position ein.
	 * 
	 * Stack: .., y, x => .., x, y, x
	 */
	public void dup_x1() {
		poppush(2, 3);
		emit(90); // dup_x1
	}

	/**
	 * Vertauscht die oberen beiden Elemente auf dem Stack
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
		// nach einem unbedingten Sprung ist die neue Stackgröße
		// unbekannt, da sie unabhängig von der Stackgröße der
		// vorherigen Anweisung ist.
		stackSize = -1;
	}

	/** Unbedingter Sprung zum angegebenen Label. */
	public void jump(Label label) {
		emitJump(167, label); // goto
		unconditionalControlTransfer();
	}

	/**
	 * Springe zum Label, wenn Wert gleich 0 ist.
	 * 
	 * Stack: .., int => ..
	 */
	public void jumpIfZero(Label label) {
		poppush(1, 0);
		emitJump(153, label); // ifeq
	}

	/**
	 * Springe zum Label, wenn Wert ungleich 0 ist.
	 * 
	 * Stack: .., int => ..
	 */
	public void jumpIfNonZero(Label label) {
		poppush(1, 0);
		emitJump(154, label); // ifne
	}

	/**
	 * Springe zum Label, wenn Referenz gleich null ist.
	 * 
	 * Stack: .., objectref => ..
	 */
	public void jumpIfNull(Label label) {
		poppush(1, 0);
		emitJump(198, label); // ifnull
	}

	/**
	 * Springe zum Label, wenn Referenze ungleich null ist.
	 * 
	 * Stack: .., objectref => ..
	 */
	public void jumpIfNonNull(Label label) {
		poppush(1, 0);
		emitJump(199, label); // ifnonnull
	}

	/**
	 * Springe zum Label, wenn Referenzen auf das gleiche Objekt zeigen.
	 * 
	 * Stack: .., obj1, obj2 => ..
	 */
	public void jumpIfReferenceEqual(Label label) {
		poppush(2, 0);
		emitJump(165, label); // if_acmpeg
	}

	/**
	 * Springe zum Label, wenn Referenzen auf unterschiedliche Objekte zeigen.
	 * 
	 * Stack: .., obj1, obj2 => ..
	 */
	public void jumpIfReferenceNotEqual(Label label) {
		poppush(2, 0);
		emitJump(166, label); // if_acmpne
	}

	/**
	 * Gibt switch aus.
	 * 
	 * @param constantValues
	 *            Die Werte, mit denen verglichen wird. Das Array muss sortiert
	 *            sein.
	 * @param targetLabels
	 *            Die Label, zu denen gesprungen wird.
	 * @param defaultLabel
	 *            Das default-Label
	 * 
	 * Stack: .., int => ..
	 */
	public void lookupSwitch(int[] constantValues, Label[] targetLabels, Label defaultLabel) {
		if (constantValues.length != targetLabels.length)
			throw new IllegalArgumentException(
					"Values and label arrays must have same length");
		if (constantValues.length == 0)
			throw new IllegalArgumentException("Empty switch not supported");
		for (int i = 0; i < constantValues.length; i++) {
			if (i > 0 && constantValues[i - 1] >= constantValues[i])
				throw new IllegalArgumentException(
						"Constants are not sorted/duplicate constant");
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
	 * Markiert die aktuelle Codestelle mit dem Label. Es sind nur
	 * Vorwärtssprünge zum Label erlaubt. Es dürfen Elements auf dem Stack
	 * liegen.
	 */
	public void markForwardJumpsOnly(Label label) {
		if (label.markedPosition == -1) {
			label.markedPosition = getCurrentPosition();
			label.allowJumps = false;
			// "synchronisiere" stackSize mit label.stackSize
			if (stackSize == -1) {
				stackSize = label.stackSize;
			} else if (label.stackSize == -1) {
				label.stackSize = stackSize;
			} else {
				if (stackSize != label.stackSize)
					throw new IllegalArgumentException(
							"Label cannot be placed here: A jump to this position expects stack size"
									+ label.stackSize + ", but stack size is "
									+ stackSize);
			}
		} else {
			throw new IllegalArgumentException(
					"The label already was used to mark a position.");
		}
	}

	/**
	 * Markiert die aktuelle Codestelle mit dem Label. Es sind Vorwärtssprünge
	 * sowie Rückwärtssprunge zum Label erlaubt. Der Stack muss während des
	 * Sprunges leer sein.
	 */
	public void mark(Label label) {
		if (label.markedPosition == -1) {
			label.markedPosition = getCurrentPosition();
			if (stackSize > 0)
				throw new IllegalArgumentException(
						"Label cannot be placed here: Stack must be empty at label position, but has size "
								+ stackSize);
			if (label.stackSize > 0)
				throw new IllegalArgumentException(
						"Label cannot be placed here: Stack must be empty during jump, but there is a jump source with stack size "
								+ label.stackSize);
			stackSize = 0;
			label.stackSize = 0;
		} else {
			throw new IllegalArgumentException(
					"The label already was used to mark a position.");
		}
	}

	/** Gibt 'return'-Instruktion aus. */
	public void returnFromProcedure() {
		emit(177); // return
		unconditionalControlTransfer();
	}

	/**
	 * Gibt 'areturn'-Instruktion aus.
	 * 
	 * Stack: .., returnvalue
	 */
	public void returnFromFunction() {
		poppush(1, 0);
		emit(176); // areturn
		unconditionalControlTransfer();
	}

	/**
	 * Liest den Wert des Feldes aus.
	 * 
	 * Stack: .., objectref => .., wert
	 */
	public void loadInstanceField(Field field) {
		if (isStatic(field))
			throw new IllegalArgumentException("Expected instance field, but found static field.");
		loadInstanceField(pool.getFieldref(field));
	}
	
	public void loadInstanceField(FieldReference field) {
		poppush(1, 1);
		emit(180); // getfield
		emitUInt16(field.index);
	}

	/**
	 * Speichert den Wert auf dem Stack in das Feld.
	 * 
	 * Stack: .., objectref, wert => ..
	 */
	public void storeInstanceField(Field field) {
		if (isStatic(field))
			throw new IllegalArgumentException("Expected instance field, but found static field.");
		storeInstanceField(pool.getFieldref(field));
	}
	
	public void storeInstanceField(FieldReference field) {
		poppush(2, 0);
		emit(181); // putfield
		emitUInt16(field.index);
	}

	/**
	 * Liest den Wert des Feldes aus.
	 * 
	 * Stack: .. => .., wert
	 */
	public void loadStaticField(Field field) {
		if (!isStatic(field))
			throw new IllegalArgumentException("Expected static field, but found instance field.");
		loadStaticField(pool.getFieldref(field));
	}
	
	public void loadStaticField(FieldReference field) {
		poppush(0, 1);
		emit(178); // getstatic
		emitUInt16(field.index);
	}

	/**
	 * Speichert den Wert auf dem Stack in das Feld.
	 * 
	 * Stack: .., wert => ..
	 */
	public void storeStaticField(Field field) {
		if (!isStatic(field))
			throw new IllegalArgumentException("Expected static field, but found instance field.");
		storeStaticField(pool.getFieldref(field));
	}
	public void storeStaticField(FieldReference field) {
		poppush(1, 0);
		emit(179); // putstatic
		emitUInt16(field.index);
	}

	static int getStackSize(Class<?> type) {
		if (type.equals(Void.TYPE))
			return 0;
		if (type.equals(Double.TYPE) || type.equals(Long.TYPE))
			return 2;
		else
			return 1;
	}

	static int getStackSize(Class<?>[] types) {
		int total = 0;
		for (Class<?> type : types)
			total += getStackSize(type);
		return total;
	}

	/**
	 * Ruft eine Instanzmethode auf.
	 * 
	 * Stack: .., objectref[, parameter1, parameter2] => ..[, returnval]
	 */
	public void invokeInstance(Method method) {
		if (isStatic(method))
			throw new IllegalArgumentException(
					"Cannot use invokeInstance for static method");
		poppush(1 + getStackSize(method.getParameterTypes()), getStackSize(method.getReturnType()));
		emit(182); // invokevirtual
		emitUInt16(pool.getMethodref(method));
	}

	/**
	 * Ruft eine statische Methode auf.
	 * 
	 * Stack: ..[, parameter1, parameter2] => ..[, returnval]
	 */
	public void invokeStatic(Method method) {
		if (!isStatic(method))
			throw new IllegalArgumentException(
					"Cannot use invokeStatic for instance method");
		poppush(getStackSize(method.getParameterTypes()), getStackSize(method.getReturnType()));
		emit(184); // invokestatic
		emitUInt16(pool.getMethodref(method));
	}

	private boolean isStatic(Member member)
	{
		return (member.getModifiers() & Modifier.STATIC) == Modifier.STATIC;
	}

	/**
	 * Erzeugt ein neues Objekt (ohne den Konstruktor aufzurufen).
	 * 
	 * Stack: .. => .., objectref
	 */
	public void newObject(Class<?> type) {
		poppush(0, 1);
		emit(187); // new
		emitUInt16(pool.getClass(type));
	}

	/**
	 * Ruft den Konstruktor eines Objektes auf.
	 * 
	 * Stack: .., objectref, parameters => ..
	 */
	public void invokeConstructor(Constructor<?> ctor)
	{
		poppush(1 + getStackSize(ctor.getParameterTypes()), 0);
		emit(183); // invokespecial
		emitUInt16(pool.getConstructor(ctor));
	}

	/**
	 * Casts an object to the target type.
	 * 
	 * Stack: obj => obj
	 */
	public void checkCast(Class<?> targetType) {
		poppush(1, 1);
		emit(192); // checkcast
		emitUInt16(pool.getClass(targetType));
	}

	/**
	 * Casts an object to the target type.
	 * 
	 * Stack: obj => obj
	 */
	public void instanceOf(Class<?> targetType) {
		poppush(1, 1);
		emit(193); // instanceof
		emitUInt16(pool.getClass(targetType));
	}
}
