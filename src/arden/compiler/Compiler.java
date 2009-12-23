package arden.compiler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import arden.codegenerator.ClassFileWriter;
import arden.codegenerator.FieldReference;
import arden.codegenerator.MethodWriter;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.MedicalLogicModuleImplementation;

/**
 * This class is responsible for generating the
 * MedicalLogicModuleImplementation-derived class.
 * 
 * @author Daniel Grunwald
 */
public class Compiler {
	private String className;
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

	public Compiler(String mlmName) {
		this.className = mlmName;
		this.classFileWriter = new ClassFileWriter(mlmName, MedicalLogicModuleImplementation.class);
	}

	/** Saves the class file */
	public void save(DataOutput output) throws IOException {
		classFileWriter.save(output);
	}

	/** Loads the Java class. */
	@SuppressWarnings("unchecked")
	public Class<? extends MedicalLogicModuleImplementation> loadClassFromMemory() {
		byte[] data;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream s = new DataOutputStream(bos);
			classFileWriter.save(s);
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
