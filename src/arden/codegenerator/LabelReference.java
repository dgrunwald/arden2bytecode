package arden.codegenerator;

class LabelReference {
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
