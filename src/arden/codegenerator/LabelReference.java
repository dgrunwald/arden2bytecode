package arden.codegenerator;

/**
 * Used internally by MethodWriter to resolve offsets between Labels. Represents
 * a place in the byte code that references a label (e.g. parameter of jump
 * instructions).
 * 
 * @author Daniel Grunwald
 */
final class LabelReference {
	/** the position of the first byte of the jump offset */
	final int referencePosition;
	/** the base position (start position of the jump instruction) */
	final int basePosition;

	final Label label;

	final boolean is32BitOffset;

	public LabelReference(int referencePosition, int basePosition, Label label, boolean is32BitOffset) {
		this.referencePosition = referencePosition;
		this.basePosition = basePosition;
		this.label = label;
		this.is32BitOffset = is32BitOffset;
	}
}
