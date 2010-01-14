package arden.compiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import arden.runtime.ArdenList;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;

final class CompiledMlm implements MedicalLogicModule {
	private Constructor<? extends MedicalLogicModuleImplementation> ctor;

	public CompiledMlm(Class<? extends MedicalLogicModuleImplementation> clazz) {
		// We know the class has an appropriate constructor because we compiled
		// it, so wrap all the checked exceptions that should never occur.
		try {
			ctor = clazz.getConstructor(ExecutionContext.class, MedicalLogicModule.class, ArdenValue[].class);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/** Creates an instance of the implementation class. */
	public MedicalLogicModuleImplementation createInstance(ExecutionContext context, ArdenValue[] arguments)
			throws InvocationTargetException {
		if (context == null)
			throw new NullPointerException();

		if (arguments == null)
			arguments = ArdenList.EMPTY.values;

		try {
			return ctor.newInstance(context, this, arguments);
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
