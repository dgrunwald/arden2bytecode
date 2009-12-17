package arden.compiler;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import arden.codegenerator.ClassFileWriter;
import arden.codegenerator.FieldReference;
import arden.codegenerator.MethodWriter;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.MedicalLogicModule;

public class Compiler {
	private ClassFileWriter classFileWriter;
	private MethodWriter staticInitializer;
	private HashMap<String, FieldReference> stringLiterals = new HashMap<String, FieldReference>();
	private HashMap<Double, FieldReference> numberLiterals = new HashMap<Double, FieldReference>();
	private int nextFieldIndex;

	private MethodWriter getStaticInitializer() {
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
				ref = classFileWriter.declareField("literal"
						+ (nextFieldIndex++), ArdenString.class,
						Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
				stringLiterals.put(value, ref);
				getStaticInitializer().newObject(ArdenString.class);
				getStaticInitializer().dup();
				getStaticInitializer().loadStringConstant(value);

				getStaticInitializer().invokeConstructor(
						ArdenString.class.getConstructor(String.class));

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
				ref = classFileWriter.declareField("literal"
						+ (nextFieldIndex++), ArdenNumber.class,
						Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
				numberLiterals.put(value, ref);
				getStaticInitializer().newObject(ArdenNumber.class);
				getStaticInitializer().dup();
				getStaticInitializer().loadDoubleConstant(value);

				getStaticInitializer().invokeConstructor(
						ArdenNumber.class.getConstructor(Double.TYPE));

				getStaticInitializer().storeStaticField(ref);
			}
			return ref;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public Compiler(String mlmName) {
		classFileWriter = new ClassFileWriter(mlmName, MedicalLogicModule.class);
	}

	/** Saves the class file */
	public void save(DataOutput output) throws IOException {
		classFileWriter.save(output);
	}
}
