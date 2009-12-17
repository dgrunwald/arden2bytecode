package arden.codegenerator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Repräsentiert den ConstantPool in einer Java .class-Datei
 * 
 * @author daniel
 * 
 */
public class ConstantPool {
	static final byte CONSTANT_Class = 7;
	static final byte CONSTANT_Fieldref = 9;
	static final byte CONSTANT_Methodref = 10;
	static final byte CONSTANT_InterfaceMethodref = 11;
	static final byte CONSTANT_String = 8;
	static final byte CONSTANT_Integer = 3;
	static final byte CONSTANT_Float = 4;
	static final byte CONSTANT_Long = 5;
	static final byte CONSTANT_Double = 6;
	static final byte CONSTANT_NameAndType = 12;
	static final byte CONSTANT_Utf8 = 1;

	static final String JAVA_CONSTRUCTOR_NAME = "<init>";

	private HashMap<Integer, Integer> integer_map = new HashMap<Integer, Integer>();
	private HashMap<Long, Integer> long_map = new HashMap<Long, Integer>();
	private HashMap<Double, Integer> double_map = new HashMap<Double, Integer>();
	private HashMap<String, Integer> utf8_map = new HashMap<String, Integer>();
	private HashMap<String, Integer> string_map = new HashMap<String, Integer>();
	private HashMap<String, Integer> class_map = new HashMap<String, Integer>();
	private HashMap<Field, FieldReference> fieldref_map = new HashMap<Field, FieldReference>();
	private HashMap<Method, Integer> methodref_map = new HashMap<Method, Integer>();
	private HashMap<NameTypePair, Integer> nameAndType_map = new HashMap<NameTypePair, Integer>();
	private HashMap<Constructor<?>, Integer> constructor_map = new HashMap<Constructor<?>, Integer>();
	private int elementNumber = 0;
	private ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
	private DataOutputStream data = new DataOutputStream(byteOutputStream);

	// gets the index for the new constant pool entry being created
	private int getNextIndex() {
		if (elementNumber >= 65534)
			throw new ClassFileLimitExceededException("Too many constants.");
		return ++elementNumber;
	}

	// gets the index for the new constant pool entry being created for a double
	// or long value
	private int getNextDoubleIndex() {
		int index = getNextIndex();
		if (elementNumber >= 65534)
			throw new ClassFileLimitExceededException("Too many constants.");
		++elementNumber; // reserve unused constant pool slot
		return index;
	}

	/** Speichert den ConstantPool in die .class-Datei */
	public void save(DataOutput output) throws IOException {
		data.flush(); // ensure the DataOutputStream writes everything to the
		// ByteArrayOutputStream
		output.writeShort(elementNumber + 1);
		output.write(byteOutputStream.toByteArray());
	}

	/**
	 * Findet einen vorhandenen Integer-Eintrag oder erstellt einen neuen.
	 * 
	 * @param value
	 *            Der Wert des integers
	 * @return Nummer des Integer-Eintrages
	 */
	public int getInteger(Integer value) {
		if (integer_map.containsKey(value))
			return integer_map.get(value);
		int index = getNextIndex();
		integer_map.put(value, index);
		try {
			data.writeByte(CONSTANT_Integer);
			data.writeInt(value);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return index;
	}

	/**
	 * Findet einen vorhandenen Double-Eintrag oder erstellt einen neuen.
	 * 
	 * @param value
	 *            Der Wert des doubles
	 * @return Nummer des Double-Eintrages
	 */
	public int getDouble(Double value) {
		if (double_map.containsKey(value))
			return double_map.get(value);
		int index = getNextDoubleIndex();
		double_map.put(value, index);
		try {
			data.writeByte(CONSTANT_Double);
			data.writeDouble(value);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return index;
	}

	/**
	 * Findet einen vorhandenen Long-Eintrag oder erstellt einen neuen.
	 * 
	 * @param value
	 *            Der Wert des longs
	 * @return Nummer des Long-Eintrages
	 */
	public int getLong(Long value) {
		if (long_map.containsKey(value))
			return long_map.get(value);
		int index = getNextDoubleIndex();
		long_map.put(value, index);
		try {
			data.writeByte(CONSTANT_Long);
			data.writeLong(value);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return index;
	}

	/**
	 * Findet einen vorhandenen Utf8-Eintrag oder erstellt einen neuen.
	 * 
	 * @param text
	 *            Der Text des Eintrages
	 * @return Nummer des Utf8-Eintrages
	 */
	public int getUtf8(String text) {
		if (utf8_map.containsKey(text))
			return utf8_map.get(text);
		int index = getNextIndex();
		utf8_map.put(text, index);
		try {
			data.writeByte(CONSTANT_Utf8);
			data.writeUTF(text);
		} catch (UTFDataFormatException ex) {
			// can occur if the text is longer than 64 KB
			throw new ClassFileLimitExceededException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return index;
	}

	/**
	 * Findet einen vorhandenen String-Eintrag oder erstellt einen neuen.
	 * 
	 * @param text
	 *            Der Text des Eintrages
	 * @return Nummer des String-Eintrages
	 */
	public int getString(String text) {
		if (string_map.containsKey(text))
			return string_map.get(text);
		int utf8 = getUtf8(text);
		int index = getNextIndex();
		string_map.put(text, index);
		try {
			data.writeByte(CONSTANT_String);
			data.writeShort(utf8);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return index;
	}

	/**
	 * Findet einen vorhandenen Class-Eintrag oder erstellt einen neuen.
	 * 
	 * @param classSym
	 *            Das Klassen-Symbol, für das der Class-Eintrag erstellt werden
	 *            soll
	 * @return Nummer des Class-Eintrages
	 */
	public int getClass(Class<?> classSym) {
		if (classSym == null)
			throw new IllegalArgumentException();
		return getClassByJavaName(getInternalJavaName(classSym));
	}

	/**
	 * Findet einen vorhandenen Class-Eintrag für einen Array-Typen oder
	 * erstellt einen neuen.
	 * 
	 * @param elementType
	 *            Der Elementtyp des Arrays
	 * @param arrayNestingLevel
	 *            Verschachtelungstiefe des Arrays
	 * @return Nummer des Class-Eintrages
	 */
	public int getClass(Class<?> elementType, int arrayNestingLevel) {
		if (elementType == null)
			throw new IllegalArgumentException();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < arrayNestingLevel; i++)
			b.append('[');
		b.append('L');
		b.append(getInternalJavaName(elementType));
		b.append(';');
		return getClassByJavaName(b.toString());
	}

	/** Ermittelt den Java-Namen der Klasse */
	static String getInternalJavaName(Class<?> classSym) {
		if (classSym.isArray() || classSym.isPrimitive())
			throw new IllegalArgumentException("Cannot use array classes or primitive classes");
		return classSym.getName().replace('.', '/');
	}

	/**
	 * Findet einen vorhandenen Class-Eintrag für einen Typen oder erstellt
	 * einen neuen.
	 * 
	 * @param internalJavaName
	 *            Der interne Java-Name des Typen (z.B. '[Lopa/runtime/Object;')
	 * @return Nummer des Class-Eintrages
	 */
	public int getClassByJavaName(String internalJavaName) {
		if (class_map.containsKey(internalJavaName))
			return class_map.get(internalJavaName);
		int utf8 = getUtf8(internalJavaName);
		int index = getNextIndex();
		class_map.put(internalJavaName, index);
		try {
			data.writeByte(CONSTANT_Class);
			data.writeShort(utf8);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return index;
	}

	/**
	 * Findet einen vorhandenen Fieldref-Eintrag oder erstellt einen neuen.
	 * 
	 * @param field
	 *            Das Feld, für das der Fieldref-Eintrag erstellt werden soll
	 * @return Nummer des Fieldref-Eintrages
	 */
	public FieldReference getFieldref(Field field) {
		if (field == null)
			throw new IllegalArgumentException();
		if (fieldref_map.containsKey(field))
			return fieldref_map.get(field);
		int classRef = getClass(field.getDeclaringClass());
		FieldReference fieldReference = createFieldref(classRef, field.getName(), field.getType());
		fieldref_map.put(field, fieldReference);
		return fieldReference;
	}

	public FieldReference createFieldref(int declaringClass, String name, Class<?> type) {
		int natRef = getNameAndType(name, createFieldDescriptor(type));
		int index = getNextIndex();
		try {
			data.writeByte(CONSTANT_Fieldref);
			data.writeShort(declaringClass);
			data.writeShort(natRef);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return new FieldReference(index);
	}

	/** Creates a field descriptor string for the type. */
	static String createFieldDescriptor(Class<?> type) {
		if (type.isPrimitive()) {
			if (type.equals(Void.TYPE))
				return "V";
			else if (type.equals(Byte.TYPE))
				return "B";
			else if (type.equals(Character.TYPE))
				return "C";
			else if (type.equals(Double.TYPE))
				return "D";
			else if (type.equals(Float.TYPE))
				return "F";
			else if (type.equals(Integer.TYPE))
				return "I";
			else if (type.equals(Long.TYPE))
				return "J";
			else if (type.equals(Short.TYPE))
				return "S";
			else if (type.equals(Boolean.TYPE))
				return "Z";
			else
				throw new RuntimeException("Unknown primitive type");
		} else if (type.isArray()) {
			return "[" + createFieldDescriptor(type.getComponentType());
		} else {
			return "L" + getInternalJavaName(type) + ";";
		}
	}

	/**
	 * Findet einen vorhandenen Methodref-Eintrag oder erstellt einen neuen.
	 * 
	 * @param method
	 *            Das Feld, für das der Methodref-Eintrag erstellt werden soll
	 * @return Nummer des Methodref-Eintrages
	 */
	public int getMethodref(Method method) {
		if (method == null)
			throw new IllegalArgumentException();
		if (methodref_map.containsKey(method))
			return methodref_map.get(method);
		int classRef = getClass(method.getDeclaringClass());
		int natRef = getNameAndType(method.getName(), createMethodDescriptor(method.getParameterTypes(), method.getReturnType()));
		int index = getNextIndex();
		methodref_map.put(method, index);
		try {
			data.writeByte(CONSTANT_Methodref);
			data.writeShort(classRef);
			data.writeShort(natRef);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return index;
	}

	/** Creates a method descriptor for the method signature. */
	static String createMethodDescriptor(Class<?>[] parameters, Class<?> returnType) {
		if (parameters.length > 254)
			throw new ClassFileLimitExceededException("Too many parameters.");
		StringBuilder b = new StringBuilder();
		b.append('(');
		for (Class<?> param : parameters) {
			b.append(createFieldDescriptor(param));
		}
		b.append(')');
		b.append(createFieldDescriptor(returnType));
		return b.toString();
	}

	/**
	 * Findet die Methodref für den Java-Konstruktor, der aus einem Java Wert
	 * einen OPA-Runtimeobjekt macht
	 * 
	 * @param ctor
	 *            Der Konstruktor.
	 * @return Nummer des Eintrags im Constant-Pool
	 */
	public int getConstructor(Constructor<?> ctor) {
		if (constructor_map.containsKey(ctor))
			return constructor_map.get(ctor);
		int classRef = getClass(ctor.getDeclaringClass());
		int natRef = getNameAndType(JAVA_CONSTRUCTOR_NAME, createMethodDescriptor(ctor.getParameterTypes(), Void.TYPE));
		int index = getNextIndex();
		constructor_map.put(ctor, index);
		try {
			data.writeByte(CONSTANT_Methodref);
			data.writeShort(classRef);
			data.writeShort(natRef);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return index;
	}

	/**
	 * Findet einen vorhandenen NameAndType-Eintrag oder erstellt einen neuen.
	 * 
	 * @param name
	 *            Der Name des NameAndType-Eintrages
	 * @param typeDescriptor
	 *            Der Typ des Eintrages (Java FieldDescriptor oder
	 *            MethodDescriptor)
	 * @return Nummer des NameAndType-Eintrages
	 */
	public int getNameAndType(String name, String typeDescriptor) {
		NameTypePair ntp = new NameTypePair(name, typeDescriptor);
		if (nameAndType_map.containsKey(ntp))
			return nameAndType_map.get(ntp);
		int nameRef = getUtf8(name);
		int descRef = getUtf8(typeDescriptor);
		int index = getNextIndex();
		nameAndType_map.put(ntp, index);
		try {
			data.writeByte(CONSTANT_NameAndType);
			data.writeShort(nameRef);
			data.writeShort(descRef);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return index;
	}

	private static class NameTypePair {
		final String name, type;

		public NameTypePair(String name, String type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof NameTypePair) {
				NameTypePair nt = (NameTypePair) o;
				return name.equals(nt.name) && type.equals(nt.type);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return name.hashCode() ^ type.hashCode();
		}
	}
}
