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

package arden.compiler;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import arden.codegenerator.ClassFileWriter;
import arden.codegenerator.FieldReference;
import arden.codegenerator.Label;
import arden.codegenerator.MethodWriter;
import arden.compiler.node.TIdentifier;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.LibraryMetadata;
import arden.runtime.MaintenanceMetadata;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;
import arden.runtime.events.EvokeEvent;

/**
 * This class is responsible for generating the
 * MedicalLogicModuleImplementation-derived class.
 * 
 * @author Daniel Grunwald
 */
final class CodeGenerator {
	private final ClassFileWriter classFileWriter;
	private MethodWriter staticInitializer;
	private final HashMap<String, FieldReference> stringLiterals = new HashMap<String, FieldReference>();
	private final HashMap<Double, FieldReference> numberLiterals = new HashMap<Double, FieldReference>();
	private final HashMap<Long, FieldReference> timeLiterals = new HashMap<Long, FieldReference>();
	private final HashMap<String, Variable> variables = new HashMap<String, Variable>();
	private int nextFieldIndex;
	private boolean isFinished;
	private FieldReference nowField;

	private static final String literalPrefix = "$literal";

	public MethodWriter getStaticInitializer() {
		if (isFinished)
			throw new IllegalStateException();
		if (staticInitializer == null) {
			staticInitializer = classFileWriter.createStaticInitializer();
			if (isDebuggingEnabled) {
				staticInitializer.enableLineNumberTable();
				staticInitializer.sequencePoint(lineNumberForStaticInitializationSequencePoint);
			}
		}
		return staticInitializer;
	}

	/**
	 * Gets a reference to the static field that stores an ArdenString with the
	 * specified value.
	 */
	public FieldReference getStringLiteral(String value) {
		try {
			FieldReference ref = stringLiterals.get(value);
			if (ref == null) {
				ref = classFileWriter.declareField(literalPrefix + (nextFieldIndex++), ArdenString.class,
						Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
				stringLiterals.put(value, ref);
				getStaticInitializer().newObject(ArdenString.class);
				getStaticInitializer().dup();
				getStaticInitializer().loadStringConstant(value);

				getStaticInitializer().invokeConstructor(ArdenString.class.getConstructor(String.class));

				getStaticInitializer().storeStaticField(ref);
			}
			return ref;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets a reference to the static field that stores an ArdenNumber with the
	 * specified value.
	 */
	public FieldReference getNumberLiteral(double value) {
		try {
			FieldReference ref = numberLiterals.get(value);
			if (ref == null) {
				ref = classFileWriter.declareField(literalPrefix + (nextFieldIndex++), ArdenNumber.class,
						Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
				numberLiterals.put(value, ref);
				getStaticInitializer().newObject(ArdenNumber.class);
				getStaticInitializer().dup();
				getStaticInitializer().loadDoubleConstant(value);

				getStaticInitializer().invokeConstructor(ArdenNumber.class.getConstructor(Double.TYPE));

				getStaticInitializer().storeStaticField(ref);
			}
			return ref;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets a reference to the static field that stores an ArdenTime with the
	 * specified value.
	 */
	public FieldReference getTimeLiteral(long value) {
		try {
			FieldReference ref = timeLiterals.get(value);
			if (ref == null) {
				ref = classFileWriter.declareField(literalPrefix + (nextFieldIndex++), ArdenTime.class,
						Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
				timeLiterals.put(value, ref);
				getStaticInitializer().newObject(ArdenTime.class);
				getStaticInitializer().dup();
				getStaticInitializer().loadLongConstant(value);

				getStaticInitializer().invokeConstructor(ArdenTime.class.getConstructor(Long.TYPE));

				getStaticInitializer().storeStaticField(ref);
			}
			return ref;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private final int lineNumberForStaticInitializationSequencePoint;

	public CodeGenerator(String mlmName, int lineNumberForStaticInitializationSequencePoint) {
		this.classFileWriter = new ClassFileWriter(mlmName, MedicalLogicModuleImplementation.class);
		this.lineNumberForStaticInitializationSequencePoint = lineNumberForStaticInitializationSequencePoint;
		createParameterLessConstructor();
	}

	private boolean isDebuggingEnabled = false;

	/** Enables debugging for the code being produced. */
	public void enableDebugging(String sourceFileName) {
		this.isDebuggingEnabled = true;
		classFileWriter.setSourceFileName(sourceFileName);
	}

	private MethodWriter ctor;
	private final Label ctorUserCodeLabel = new Label();
	private final Label ctorInitCodeLabel = new Label();
	private int lineNumberForInitializationSequencePoint;

	private MethodWriter parameterLessCtor;
	
	public CompilerContext createConstructor(int lineNumberForInitializationSequencePoint) {
		ctor = classFileWriter.createConstructor(Modifier.PUBLIC, new Class<?>[] { ExecutionContext.class,
				MedicalLogicModule.class, ArdenValue[].class });
		this.lineNumberForInitializationSequencePoint = lineNumberForInitializationSequencePoint;
		if (isDebuggingEnabled) {
			ctor.enableLineNumberTable();
			ctor.sequencePoint(lineNumberForStaticInitializationSequencePoint);
		}
		ctor.loadThis();
		try {
			ctor.invokeConstructor(MedicalLogicModuleImplementation.class.getConstructor());
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		ctor.jump(ctorInitCodeLabel);
		ctor.mark(ctorUserCodeLabel);
		return new CompilerContext(this, ctor, 3);
	}
	
	public CompilerContext createParameterLessConstructor() {
		parameterLessCtor = classFileWriter.createConstructor(Modifier.PUBLIC, new Class<?>[] {});
		parameterLessCtor.loadThis();
		try {
			parameterLessCtor.invokeConstructor(MedicalLogicModuleImplementation.class.getConstructor());
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return new CompilerContext(this, ctor, 3);
	}

	public CompilerContext createLogic() {
		MethodWriter w = classFileWriter.createMethod("logic", Modifier.PUBLIC,
				new Class<?>[] { ExecutionContext.class }, Boolean.TYPE);
		if (isDebuggingEnabled)
			w.enableLineNumberTable();
		return new CompilerContext(this, w, 1);
	}

	public CompilerContext createAction() {
		MethodWriter w = classFileWriter.createMethod("action", Modifier.PUBLIC,
				new Class<?>[] { ExecutionContext.class }, ArdenValue[].class);
		if (isDebuggingEnabled)
			w.enableLineNumberTable();
		return new CompilerContext(this, w, 1);
	}

	public CompilerContext createUrgency() {
		MethodWriter w = classFileWriter.createMethod("getUrgency", Modifier.PUBLIC, new Class<?>[] {}, Double.TYPE);
		if (isDebuggingEnabled)
			w.enableLineNumberTable();
		return new CompilerContext(this, w, 0);
	}
	
	public CompilerContext createPriority() {
		MethodWriter w = classFileWriter.createMethod("getPriority", Modifier.PUBLIC, new Class<?>[] {}, Double.TYPE);
		if (isDebuggingEnabled)
			w.enableLineNumberTable();
		return new CompilerContext(this, w, 0);
	}
	
	public CompilerContext createMaintenance() {
		MethodWriter w = classFileWriter.createMethod("getMaintenanceMetadata", Modifier.PUBLIC, new Class<?>[] {}, MaintenanceMetadata.class);
		if (isDebuggingEnabled)
			w.enableLineNumberTable();
		return new CompilerContext(this, w, 0);
	}
	
	public CompilerContext createLibrary() {
		MethodWriter w = classFileWriter.createMethod("getLibraryMetadata", Modifier.PUBLIC, new Class<?>[] {}, LibraryMetadata.class);
		if (isDebuggingEnabled)
			w.enableLineNumberTable();
		return new CompilerContext(this, w, 0);
	}
	
	public CompilerContext createEvokeEvent() {
		MethodWriter w = classFileWriter.createMethod(
				"getEvokeEvent", 
				Modifier.PUBLIC, 
				new Class<?>[] { ExecutionContext.class }, 
				EvokeEvent.class);
		if (isDebuggingEnabled)
			w.enableLineNumberTable();
		return new CompilerContext(this, w, 1);
	}
	
	public void createGetValue() {
		MethodWriter w = classFileWriter.createMethod(
				"getValue", 
				Modifier.PUBLIC, 
				new Class<?>[]{ String.class }, 
				ArdenValue.class);
		try {
			Label excptBegin = new Label();
			Label excptEnd = new Label();
			Label end = new Label();
			Label secHandler = new Label();
			Label noSuchFieldHandler = new Label();
			Label illegalArgHandler = new Label();
			Label illegalAccHandler = new Label();
			w.mark(excptBegin);
			w.loadThis();
			w.invokeInstance(Object.class.getMethod("getClass"));
			w.loadVariable(1);
			w.invokeInstance(Class.class.getMethod("getDeclaredField", String.class));
			w.dup();
			w.storeVariable(2);
			w.loadThis();
			w.invokeInstance(Field.class.getMethod("get", Object.class));
			w.checkCast(ArdenValue.class);
			w.returnObjectFromFunction();
			w.mark(excptEnd);
			
			w.markExceptionHandler(secHandler);
			w.storeVariable(3);
			w.jump(end);
			
			w.markExceptionHandler(noSuchFieldHandler);
			w.storeVariable(3);
			w.jump(end);
			
			w.markExceptionHandler(illegalArgHandler);
			w.storeVariable(3);
			w.jump(end);
			
			w.markExceptionHandler(illegalAccHandler);
			w.storeVariable(3);
			w.jump(end);
			
			w.mark(end);
			w.loadNull();
			w.returnObjectFromFunction();
			
			w.addExceptionInfo(excptBegin, excptEnd, noSuchFieldHandler, NoSuchFieldException.class);
			w.addExceptionInfo(excptBegin, excptEnd, secHandler, SecurityException.class);
			w.addExceptionInfo(excptBegin, excptEnd, illegalAccHandler, IllegalAccessException.class);
			w.addExceptionInfo(excptBegin, excptEnd, illegalArgHandler, IllegalArgumentException.class);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public FieldReference getNowField() {
		if (nowField == null) {
			nowField = classFileWriter.declareField("now", ArdenValue.class, Modifier.PRIVATE);
		}
		return nowField;
	}

	public FieldReference createField(String name, Class<?> type, int modifiers) {
		return classFileWriter.declareField(name, type, modifiers);
	}

	private int formatFieldCount;

	public FieldReference createStaticFinalField(Class<?> type) {
		return classFileWriter.declareField("format$" + (++formatFieldCount), type, Modifier.PRIVATE | Modifier.STATIC
				| Modifier.FINAL);
	}

	private ArrayList<FieldReference> fieldsNeedingInitialization = new ArrayList<FieldReference>();

	/**
	 * Creates a field of type ArdenValue that is initialized to
	 * ArdenNull.INSTANCE.
	 */
	public FieldReference createInitializedField(String name, int modifiers) {
		FieldReference f = classFileWriter.declareField(name, ArdenValue.class, modifiers);
		fieldsNeedingInitialization.add(f);
		return f;
	}

	/** Gets the variable with the specified name, or null if it does not exist. */
	public Variable getVariable(String name) {
		return variables.get(name.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Gets the variable with the specified name, or throws a
	 * RuntimeCompilerException if it does not exist.
	 */
	public Variable getVariableOrShowError(TIdentifier identifier) {
		Variable var = getVariable(identifier.getText());
		if (var == null)
			throw new RuntimeCompilerException(identifier, "Unknown variable: " + identifier.getText());
		return var;
	}

	/** Creates a new variable. */
	public void addVariable(Variable var) {
		if (getVariable(var.name) != null)
			throw new RuntimeCompilerException(var.definitionPosition, "A variable with the name '" + var.name
					+ "' already exists.");
		variables.put(var.name.toLowerCase(Locale.ENGLISH), var);
	}

	public void deleteVariable(Variable var) {
		if (getVariable(var.name) != var)
			throw new RuntimeException("Cannot delete variable that does not exist");
		variables.remove(var.name.toLowerCase(Locale.ENGLISH));
	}

	/** Saves the class file */
	public void save(DataOutput output) throws IOException {
		if (!isFinished) {
			if (parameterLessCtor != null) {
				parameterLessCtor.returnFromProcedure();
			}
			
			if (staticInitializer != null)
				staticInitializer.returnFromProcedure();

			ctor.sequencePoint(lineNumberForInitializationSequencePoint);
			ctor.returnFromProcedure();
			ctor.markForwardJumpsOnly(ctorInitCodeLabel);
			if (nowField != null) {
				ctor.loadThis();
				ctor.loadVariable(1);
				ctor.invokeInstance(ExecutionContextMethods.getCurrentTime);
				ctor.storeInstanceField(nowField);
			}
			for (FieldReference fieldToInit : fieldsNeedingInitialization) {
				ctor.loadThis();
				try {
					ctor.loadStaticField(ArdenNull.class.getField("INSTANCE"));
				} catch (SecurityException e) {
					throw new RuntimeException(e);
				} catch (NoSuchFieldException e) {
					throw new RuntimeException(e);
				}
				ctor.storeInstanceField(fieldToInit);
			}

			ctor.jump(ctorUserCodeLabel);

			isFinished = true;
		}
		classFileWriter.save(output);
	}
}
