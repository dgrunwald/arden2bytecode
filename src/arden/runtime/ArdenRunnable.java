package arden.runtime;

import java.lang.reflect.InvocationTargetException;

/** 
 * 
 * Represents an executable entity (example: MedicalLogicModule).
 * 
 * @author Daniel Grunwald
 *
 */
public interface ArdenRunnable {
	/**
	 * Executes the MLM.
	 * 
	 * @return Returns the value(s) provided by the "return" statement, or
	 *         (Java) null if no return statement was executed.
	 */
	ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments) throws InvocationTargetException;
}
