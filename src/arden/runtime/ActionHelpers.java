package arden.runtime;

/**
 * Static helper methods for ActionCompiler.
 * 
 * @author Daniel Grunwald
 */
public class ActionHelpers {
	public static void write(ExecutionContext context, ArdenValue value) {
		context.write(value);
	}
}
