package arden.codegenerator;

/**
 * This exceptions occurs when one of the limits of the .class file format is
 * exceeded.
 * 
 * @author Daniel Grunwald
 */
public class ClassFileLimitExceededException extends RuntimeException {

	private static final long serialVersionUID = -8632386917620523076L;

	public ClassFileLimitExceededException() {
	}

	public ClassFileLimitExceededException(String message) {
		super(message);
	}

	public ClassFileLimitExceededException(Exception inner) {
		super(inner);
	}
}
