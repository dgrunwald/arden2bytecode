package arden.codegenerator;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for writing .class files
 * 
 * @author Daniel Grunwald
 */
public final class ClassFileWriter {
	ConstantPool pool = new ConstantPool();

	int this_class;
	int super_class;

	/** Creates a new ClassFileWriter for writing the specified class */
	public ClassFileWriter(String className, Class<?> superClass) {
		if (className == null)
			throw new IllegalArgumentException();
		this_class = pool.getClassByJavaName(className.replace('.', '/'));
		super_class = pool.getClass(superClass);
	}

	class AttributeInfo {
		int nameIndex;
		byte[] data;

		public AttributeInfo(String name) {
			nameIndex = pool.getUtf8(name);
		}

		void save(DataOutput output) throws IOException {
			output.writeShort(nameIndex);
			output.writeInt(data.length);
			output.write(data);
		}
	}

	class FieldInfo {
		short access_flags;
		int name_index;
		int descriptor_index;

		FieldInfo(String name, Class<?> type, int modifiers) {
			access_flags = (short) modifiers;
			name_index = pool.getUtf8(name);
			descriptor_index = pool.getUtf8(ConstantPool.createFieldDescriptor(type));
		}

		void save(DataOutput output) throws IOException {
			output.writeShort(access_flags);
			output.writeShort(name_index);
			output.writeShort(descriptor_index);
			output.writeShort(0); // attributes_count
		}
	}

	List<FieldInfo> fields = new ArrayList<FieldInfo>();

	/** Declares a new field in the class */
	public FieldReference declareField(String name, Class<?> type, int modifiers) {
		if (fields.size() >= 65534)
			throw new ClassFileLimitExceededException("Too many fields.");
		fields.add(new FieldInfo(name, type, modifiers));
		return pool.createFieldref(this_class, name, type);
	}

	class MethodInfo {
		short access_flags;
		int name_index;
		int descriptor_index;
		MethodWriter writer;
		AttributeInfo codeAttribute;

		MethodInfo(String name, int modifiers, Class<?>[] parameters, Class<?> returnType) {
			writer = new MethodWriter(pool, (modifiers & Modifier.STATIC) != Modifier.STATIC, parameters.length);
			access_flags = (short) modifiers;
			name_index = pool.getUtf8(name);
			descriptor_index = pool.getUtf8(ConstantPool.createMethodDescriptor(parameters, returnType));
			codeAttribute = new AttributeInfo("Code");
		}

		void save(DataOutput output) throws IOException {
			output.writeShort(access_flags);
			output.writeShort(name_index);
			output.writeShort(descriptor_index);
			output.writeShort(1); // attributes_count
			codeAttribute.data = writer.getCodeAttributeData();
			codeAttribute.save(output);
		}
	}

	static final String JAVA_CONSTRUCTOR_NAME = "<init>";
	static final String JAVA_STATIC_INITIALIZER_NAME = "<clinit>";

	List<MethodInfo> methods = new ArrayList<MethodInfo>();

	/** Creates a new method in the class */
	public MethodWriter createMethod(String name, int modifiers, Class<?>[] parameters, Class<?> returnType) {
		if (methods.size() >= 65534)
			throw new ClassFileLimitExceededException("Too many methods.");
		MethodInfo info = new MethodInfo(name, modifiers, parameters, returnType);
		methods.add(info);
		return info.writer;
	}

	public MethodWriter createConstructor(int modifiers, Class<?>[] parameters) {
		return createMethod(JAVA_CONSTRUCTOR_NAME, modifiers, parameters, Void.TYPE);
	}

	public MethodWriter createStaticInitializer() {
		return createMethod(JAVA_STATIC_INITIALIZER_NAME, Modifier.PUBLIC | Modifier.STATIC, new Class<?>[0], Void.TYPE);
	}

	/** Saves the class file to disk */
	public void save(String filename) throws IOException {
		DataOutputStream s = new DataOutputStream(new FileOutputStream(filename));
		try {
			save(s);
		} finally {
			s.close();
		}
	}

	/** Saves the class file */
	public void save(DataOutput output) throws IOException {
		output.writeInt(0xCAFEBABE); // magic
		output.writeShort(0x00); // minor_version
		output.writeShort(0x32); // major_version
		pool.save(output);
		output.writeShort(0x0021); // ACC_SUPER | ACC_PUBLIC
		output.writeShort(this_class);
		output.writeShort(super_class);
		output.writeShort(0); // interfaces_count
		output.writeShort(fields.size()); // fields_count
		for (FieldInfo info : fields)
			info.save(output);
		output.writeShort(methods.size()); // methods_count
		for (MethodInfo info : methods)
			info.save(output);
		output.writeShort(0); // attributes_count
	}
}
