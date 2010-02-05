package arden.runtime;

import java.lang.reflect.InvocationTargetException;

/**
 * Represents a compiled medical logic module.
 * 
 * @author Daniel Grunwald
 */
public interface MedicalLogicModule extends ArdenRunnable {
	/** Creates a new instance of the implementation class. */
	MedicalLogicModuleImplementation createInstance(ExecutionContext context, ArdenValue[] arguments)
			throws InvocationTargetException;

	/** Gets the mlmname */
	String getName();

	/** Gets the maintenance metadata */
	MaintenanceMetadata getMaintenance();

	/** Gets the library metadata */
	LibraryMetadata getLibrary();

	/** Gets the priority of this module. */
	double getPriority();
}
