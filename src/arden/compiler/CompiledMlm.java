package arden.compiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;

final class CompiledMlm implements MedicalLogicModule {
	private Class<? extends MedicalLogicModuleImplementation> clazz;

	public CompiledMlm(Class<? extends MedicalLogicModuleImplementation> clazz) {
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
	 * @return Returns the value(s) provided by the "return" statement, or
	 *         (Java) null if no return statement was executed.
	 */
	public ArdenValue[] run(ExecutionContext context) throws InvocationTargetException {
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
