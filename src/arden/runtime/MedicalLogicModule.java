package arden.runtime;

import java.lang.reflect.InvocationTargetException;

/**
 * Represents a compiled medical logic module.
 * 
 * @author Daniel Grunwald
 */
public interface MedicalLogicModule {
	/** Creates a new instance of the implementation class. */
	MedicalLogicModuleImplementation createInstance(ExecutionContext context) throws InvocationTargetException;

	/**
	 * Executes the MLM.
	 * 
	 * @return Returns the value(s) provided by the "return" statement, or
	 *         (Java) null if no return statement was executed.
	 */
	ArdenValue[] run(ExecutionContext context) throws InvocationTargetException;
}
