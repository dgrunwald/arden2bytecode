package arden.compiler;

import arden.compiler.node.PExpr;
import arden.compiler.node.Token;

/**
 * Represents a Variable (anything that can be named by an identifier)
 * 
 * @author Daniel Grunwald
 */
abstract class Variable {
	final String name;

	public Variable(String name) {
		this.name = name;
	}

	/**
	 * Emits the instructions to load the variable's value.
	 * 
	 * Stack: .. => .., value
	 */
	public void loadValue(CompilerContext context, Token errorPosition) {
		throw new RuntimeCompilerException(errorPosition, "The variable '" + name + "' cannot be read from.");
	}

	/**
	 * Emits the instructions to save a value to the variable.
	 * 
	 * Stack: .., value => ..
	 */
	public void saveValue(CompilerContext context, Token errorPosition) {
		throw new RuntimeCompilerException(errorPosition, "The variable '" + name + "' cannot be written to.");
	}

	/**
	 * Emits a call to the variable. This causes code for 'arguments' and
	 * 'delay' to be generated.
	 * 
	 * Stack: .. => .., returnValue
	 */
	public void call(CompilerContext context, Token errorPosition, PExpr arguments, PExpr delay) {
		throw new RuntimeCompilerException(errorPosition, "The variable '" + name + "' cannot be called.");
	}
}
