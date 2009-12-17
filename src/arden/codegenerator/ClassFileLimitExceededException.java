package arden.codegenerator;

/**
 * Diese Exception tritt auf, wenn eines der Limits des Class-File-Formates zu
 * überschreiten.
 * 
 * @author daniel
 * 
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
