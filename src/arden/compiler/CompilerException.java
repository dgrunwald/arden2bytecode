package arden.compiler;

import arden.compiler.lexer.LexerException;
import arden.compiler.parser.ParserException;

/**
 * This exception is used to report compiler errors.
 * 
 * @author Daniel Grunwald
 */
public class CompilerException extends Exception {
	private static final long serialVersionUID = -4674298085134149649L;

	private final int line, pos;

	public CompilerException(ParserException innerException) {
		super(innerException);
		if (innerException.getToken() != null) {
			this.line = innerException.getToken().getLine();
			this.pos = innerException.getToken().getPos();
		} else {
			this.line = 0;
			this.pos = 0;
		}
	}

	public CompilerException(LexerException innerException) {
		super(innerException);
		this.line = 0;
		this.pos = 0;
	}

	CompilerException(RuntimeCompilerException innerException) {
		super(innerException.getMessage(), innerException);
		this.line = innerException.line;
		this.pos = innerException.pos;
	}

	public CompilerException(String message, int pos, int line) {
		super(message);
		this.line = line;
		this.pos = pos;
	}

	public int getLine() {
		return line;
	}

	public int getPos() {
		return pos;
	}
}
