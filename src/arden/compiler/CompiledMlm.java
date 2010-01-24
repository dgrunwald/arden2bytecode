package arden.compiler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import arden.runtime.ArdenList;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;

public final class CompiledMlm implements MedicalLogicModule {
	private final byte[] data;
	private Constructor<? extends MedicalLogicModuleImplementation> ctor;

	CompiledMlm(byte[] data) {
		if (data == null)
			throw new NullPointerException();
		this.data = data;
	}

	public void saveClassFile(OutputStream os) throws IOException {
		os.write(data);
	}

	@SuppressWarnings("unchecked")
	private synchronized Constructor<? extends MedicalLogicModuleImplementation> getConstructor() {
		if (ctor == null) {
			Class<? extends MedicalLogicModuleImplementation> clazz;
			try {
				ClassLoader classLoader = new InMemoryClassLoader("xyz", data);
				clazz = (Class<? extends MedicalLogicModuleImplementation>) classLoader.loadClass("xyz");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			// We know the class has an appropriate constructor because we
			// compiled it, so wrap all the checked exceptions that should never
			// occur.
			try {
				ctor = clazz.getConstructor(ExecutionContext.class, MedicalLogicModule.class, ArdenValue[].class);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		return ctor;
	}

	/** Creates an instance of the implementation class. */
	public MedicalLogicModuleImplementation createInstance(ExecutionContext context, ArdenValue[] arguments)
			throws InvocationTargetException {
		if (context == null)
			throw new NullPointerException();

		if (arguments == null)
			arguments = ArdenList.EMPTY.values;

		try {
			return getConstructor().newInstance(context, this, arguments);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Executes the MLM.
	 * 
	 * @return Returns the value(s) provided by the "return" statement, or
	 *         (Java) null if no return statement was executed.
	 */
	public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments) throws InvocationTargetException {
		MedicalLogicModuleImplementation impl = createInstance(context, arguments);
		try {
			if (impl.logic(context))
				return impl.action(context);
			else
				return null;
		} catch (Exception ex) {
			throw new InvocationTargetException(ex);
		}
	}
}
