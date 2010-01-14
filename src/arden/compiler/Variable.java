package arden.compiler;

import arden.compiler.node.PExpr;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.Token;

/**
 * Represents a Variable (anything that can be named by an identifier).
 * Normal variables are DataVariables, other classes derived from Variable are used for special variable types.
 * 
 * @author Daniel Grunwald
 */
abstract class Variable {
	final String name;
	final Token definitionPosition;

	public Variable(String name, Token definitionPosition) {
		this.name = name;
		this.definitionPosition = definitionPosition;
	}

	public Variable(TIdentifier name) {
		this.name = name.getText();
		this.definitionPosition = name;
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
	 * Emits a call to the variable. This causes code for 'arguments' to be
	 * generated.
	 * 
	 * Stack: .. => .., returnValues[]
	 */
	public void call(CompilerContext context, Token errorPosition, PExpr arguments) {
		throw new RuntimeCompilerException(errorPosition, "The variable '" + name + "' cannot be called.");
	}

	/**
	 * Emits a delayed call to the variable. This causes code for 'arguments'
	 * and 'delay' to be generated.
	 * 
	 * Stack: .. => ..
	 */
	public void callWithDelay(CompilerContext context, Token errorPosition, PExpr arguments, PExpr delay) {
		throw new RuntimeCompilerException(errorPosition, "The variable '" + name + "' cannot be called with DELAY.");
	}
}
