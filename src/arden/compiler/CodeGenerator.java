package arden.compiler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
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
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;

/**
 * This class is responsible for generating the
 * MedicalLogicModuleImplementation-derived class.
 * 
 * @author Daniel Grunwald
 */
final class CodeGenerator {
	private String className;
	private ClassFileWriter classFileWriter;
	private MethodWriter staticInitializer;
	private HashMap<String, FieldReference> stringLiterals = new HashMap<String, FieldReference>();
	private HashMap<Double, FieldReference> numberLiterals = new HashMap<Double, FieldReference>();
	private HashMap<Long, FieldReference> timeLiterals = new HashMap<Long, FieldReference>();
	private HashMap<String, Variable> variables = new HashMap<String, Variable>();
	private int nextFieldIndex;
	private boolean isFinished;
	private FieldReference nowField;

	private MethodWriter getStaticInitializer() {
		if (isFinished)
			throw new IllegalStateException();
		if (staticInitializer == null) {
			staticInitializer = classFileWriter.createStaticInitializer();
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
				ref = classFileWriter.declareField("literal" + (nextFieldIndex++), ArdenString.class, Modifier.PRIVATE
						| Modifier.STATIC | Modifier.FINAL);
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
				ref = classFileWriter.declareField("literal" + (nextFieldIndex++), ArdenNumber.class, Modifier.PRIVATE
						| Modifier.STATIC | Modifier.FINAL);
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
				ref = classFileWriter.declareField("literal" + (nextFieldIndex++), ArdenTime.class, Modifier.PRIVATE
						| Modifier.STATIC | Modifier.FINAL);
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

	public CodeGenerator(String mlmName) {
		this.className = mlmName;
		this.classFileWriter = new ClassFileWriter(mlmName, MedicalLogicModuleImplementation.class);
	}

	MethodWriter ctor;
	Label ctorUserCodeLabel = new Label();
	Label ctorInitCodeLabel = new Label();

	public CompilerContext createConstructor() {
		ctor = classFileWriter.createConstructor(Modifier.PUBLIC, new Class<?>[] { ExecutionContext.class,
				MedicalLogicModule.class, ArdenValue[].class });
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

	public CompilerContext createLogic() {
		MethodWriter w = classFileWriter.createMethod("logic", Modifier.PUBLIC,
				new Class<?>[] { ExecutionContext.class }, Boolean.TYPE);
		return new CompilerContext(this, w, 1);
	}

	public CompilerContext createAction() {
		MethodWriter w = classFileWriter.createMethod("action", Modifier.PUBLIC,
				new Class<?>[] { ExecutionContext.class }, ArdenValue[].class);
		return new CompilerContext(this, w, 1);
	}

	public CompilerContext createUrgency() {
		MethodWriter w = classFileWriter.createMethod("getUrgency", Modifier.PUBLIC, new Class<?>[] {}, Double.TYPE);
		return new CompilerContext(this, w, 0);
	}

	public FieldReference getNowField() {
		if (nowField == null) {
			nowField = classFileWriter.declareField("now", ArdenValue.class, Modifier.PRIVATE | Modifier.FINAL);
		}
		return nowField;
	}

	public FieldReference createField(String name, Class<?> type, int modifiers) {
		return classFileWriter.declareField(name, type, modifiers);
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
			if (staticInitializer != null)
				staticInitializer.returnFromProcedure();

			ctor.returnFromProcedure();
			ctor.markForwardJumpsOnly(ctorInitCodeLabel);
			if (nowField != null) {
				ctor.loadThis();
				ctor.loadVariable(1);
				ctor.invokeInstance(ExecutionContextMethods.getCurrentTime);
				ctor.storeInstanceField(nowField);
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
			}
			ctor.jump(ctorUserCodeLabel);

			isFinished = true;
		}
		classFileWriter.save(output);
	}

	/** Loads the Java class. */
	@SuppressWarnings("unchecked")
	public Class<? extends MedicalLogicModuleImplementation> loadClassFromMemory() {
		byte[] data;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream s = new DataOutputStream(bos);
			save(s);
			s.close();
			data = bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			ClassLoader classLoader = new InMemoryClassLoader(className, data);
			return (Class<? extends MedicalLogicModuleImplementation>) classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
