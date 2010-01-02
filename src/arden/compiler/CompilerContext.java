package arden.compiler;

import arden.codegenerator.MethodWriter;

/**
 * Holds the context for the compilation of a method.
 * 
 * @author Daniel Grunwald
 */
final class CompilerContext {
	public final CodeGenerator codeGenerator;
	public final MethodWriter writer;
	public final int executionContextVariable = 1;
	private int currentItVariable = 1; // 2...inf

	public CompilerContext(CodeGenerator codeGenerator, MethodWriter writer) {
		this.codeGenerator = codeGenerator;
		this.writer = writer;
	}

	/** Allocates a new 'it' variable and pushes it onto stack */
	public int allocateItVariable() {
		return ++currentItVariable;
	}

	/**
	 * Gets the current 'it' variable, or -1 if there is no active 'it' variable
	 */
	public int getCurrentItVariable() {
		if (currentItVariable <= executionContextVariable)
			return -1;
		return currentItVariable;
	}

	/** Frees the current 'it' variable. */
	public void popItVariable() {
		if (currentItVariable <= executionContextVariable)
			throw new RuntimeException("no 'it' variable allocated");
		currentItVariable--;
	}
}
