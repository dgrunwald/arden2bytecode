package arden.compiler;

import arden.compiler.node.Token;

/**
 * Used inside the compiler as CompilerException replacement where checked
 * exceptions are not allowed due to the visitor pattern
 * 
 * @author Daniel Grunwald
 */
@SuppressWarnings("serial")
class RuntimeCompilerException extends RuntimeException {
	final int line, pos;

	public RuntimeCompilerException(Token position, String message) {
		super(message);
		this.line = position.getLine();
		this.pos = position.getPos();
	}

	public RuntimeCompilerException(String message) {
		super(message);
		this.line = 0;
		this.pos = 0;
	}
}
