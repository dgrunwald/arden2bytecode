package arden.runtime;

/**
 * Static helper methods for ActionCompiler.
 * 
 * @author Daniel Grunwald
 */
public final class ActionHelpers {
	public static void write(ExecutionContext context, ArdenValue value) {
		context.write(value);
	}
}
