package arden.compiler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import arden.runtime.ArdenList;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.LibraryMetadata;
import arden.runtime.MaintenanceMetadata;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;

/**
 * Represents a compiled MedicalLogicModule.
 * 
 * Allows saving the compiled bytecode into a .class file by calling the
 * saveClassFile() method.
 * 
 * When createInstance() or run() is called, the compiled bytecode is loaded
 * using the InMemoryClassLoader for execution.
 * 
 * @author Daniel Grunwald
 * 
 */
public final class CompiledMlm implements MedicalLogicModule {
	private final byte[] data;
	private final MaintenanceMetadata maintenance;
	private final LibraryMetadata library;
	private final String mlmname;
	private final double priority;
	private Constructor<? extends MedicalLogicModuleImplementation> ctor;

	CompiledMlm(byte[] data, MaintenanceMetadata maintenance, LibraryMetadata library, double priority) {
		if (data == null || maintenance == null || library == null)
			throw new NullPointerException();
		this.data = data;
		this.maintenance = maintenance;
		this.library = library;
		this.mlmname = maintenance.getMlmName();
		this.priority = priority;
	}

	public void saveClassFile(OutputStream os) throws IOException {
		os.write(data);
	}

	@SuppressWarnings("unchecked")
	private synchronized Constructor<? extends MedicalLogicModuleImplementation> getConstructor() {
		if (ctor == null) {
			Class<? extends MedicalLogicModuleImplementation> clazz;
			try {
				ClassLoader classLoader = new InMemoryClassLoader(mlmname, data);
				clazz = (Class<? extends MedicalLogicModuleImplementation>) classLoader.loadClass(mlmname);
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
	@Override
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
	@Override
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

	@Override
	public MaintenanceMetadata getMaintenance() {
		return maintenance;
	}

	@Override
	public LibraryMetadata getLibrary() {
		return library;
	}

	@Override
	public String getName() {
		return mlmname;
	}

	@Override
	public double getPriority() {
		return priority;
	}
}
