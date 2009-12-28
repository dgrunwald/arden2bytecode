package arden.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Represents a compiled medical logic module.
 * 
 * @author Daniel Grunwald
 */
public class MedicalLogicModule {
	Class<? extends MedicalLogicModuleImplementation> clazz;

	public MedicalLogicModule(Class<? extends MedicalLogicModuleImplementation> clazz) {
		if (clazz == null)
			throw new IllegalArgumentException();
		this.clazz = clazz;
	}

	/** Creates an instance of the implementation class. */
	public MedicalLogicModuleImplementation createInstance(ExecutionContext context) throws InvocationTargetException {
		if (context == null)
			throw new IllegalArgumentException();

		// We know the class has an appropriate constructor because we compiled
		// it, so wrap all the checked exceptions that should never occur.
		Constructor<? extends MedicalLogicModuleImplementation> ctor;
		try {
			ctor = clazz.getConstructor(ExecutionContext.class);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		try {
			return ctor.newInstance(context);
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
	 * @return Returns the value(s) provided by the "return" statement, or (Java)
	 *         null if no return statement was executed.
	 */
	public ArdenValue[] run(ExecutionContext context) throws InvocationTargetException
	{
		MedicalLogicModuleImplementation impl = createInstance(context);
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
