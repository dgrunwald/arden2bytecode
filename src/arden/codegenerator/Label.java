package arden.codegenerator;

/**
 * Represents a jump label inside the byte code.
 * 
 * Usage: <br> 
 * {@code Label label = new Label(); } <br> 
 * {@code methodWriter.jump(label); // Jump forwards (target label not yet defined) } <br> 
 * {@code ... // generate more Code } <br>
 * {@code methodWriter.mark(label); // Place jump label }
 * 
 * @author Daniel Grunwald
 * 
 */
public final class Label {
	/** Target position (byte index where the label is pointing to), -1=not yet set */
	int markedPosition = -1;
	/** Stack size at target position, -1=currently unknown */
	int stackSize = -1;
	/** Initially true, is set to false by markForwardOnly() to signal that backward jumps are forbidden */
	boolean allowJumps = true;
}
