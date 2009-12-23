package arden.runtime;

/**
 * Base class for compiled logic etc. The compiler creates derived classes. An
 * instance of the derived class will be created whenever
 * 
 * @author Daniel Grunwald
 */
public abstract class MedicalLogicModuleImplementation {
	public MedicalLogicModuleImplementation(ExecutionContext context) {
		// All derived classes are expected to have a constructor taking a
		// context,
		// which should call this constructor.
		if (context == null)
			throw new IllegalArgumentException();
	}
	
	public abstract boolean logic(ExecutionContext context);
	public abstract ArdenValue action(ExecutionContext context);
}
