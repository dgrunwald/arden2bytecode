package arden.compiler;

import java.util.Stack;

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
	private int nextFreeVariable = 2;
	/** Stack of currently active 'it' variables */
	private Stack<Integer> itVariables = new Stack<Integer>();
	/** Stack of 'it' variables that are free for reuse */
	private Stack<Integer> freeItVariables = new Stack<Integer>();

	public CompilerContext(CodeGenerator codeGenerator, MethodWriter writer) {
		this.codeGenerator = codeGenerator;
		this.writer = writer;
	}

	/** Allocates a new variable slot in the current Java method. */
	public int allocateVariable() {
		return nextFreeVariable++;
	}

	/** Allocates a new 'it' variable and pushes it onto stack */
	public int allocateItVariable() {
		int var = freeItVariables.empty() ? allocateVariable() : freeItVariables.pop();
		itVariables.push(var);
		return var;
	}

	/**
	 * Gets the current 'it' variable, or -1 if there is no active 'it' variable
	 */
	public int getCurrentItVariable() {
		if (itVariables.empty())
			return -1;
		return itVariables.peek();
	}

	/** Frees the current 'it' variable. */
	public void popItVariable() {
		freeItVariables.push(itVariables.pop());
	}
}
