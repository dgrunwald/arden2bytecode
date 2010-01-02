package arden.codegenerator;

/**
 * Represents a field referenced in the constant pool.
 * 
 * @author Daniel Grunwald
 */
public final class FieldReference {
	final int index;

	FieldReference(int index) {
		this.index = index;
	}
}
