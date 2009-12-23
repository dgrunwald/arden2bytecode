package arden.compiler;

/**
 * This exception is used to report compiler errors.
 * 
 * @author Daniel Grunwald
 */
public class CompilerException extends Exception {
	private static final long serialVersionUID = -4674298085134149649L;

	public CompilerException(Throwable innerException) {
		super(innerException);
	}
	
	public CompilerException(String message) {
		super(message);
	}
}
