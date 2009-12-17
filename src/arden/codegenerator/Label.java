package arden.codegenerator;

/**
 * Repräsentiert eine Sprungmarke im Bytecode.
 * 
 * Verwendung: <br> 
 * {@code Label marke = new Label(); } <br> 
 * {@code methodWriter.jump(marke); // Springe vorwärts (zu noch nicht definierter
 * Marke) } <br> 
 * {@code ... // Generiere mehr Code } <br>
 * {@code methodWriter.mark(marke); // Plaziere Sprungmarke }
 * 
 * @author daniel
 * 
 */
public class Label {
	/** Zielposition, auf die das Label zeigt, -1=noch nicht gesetzt */
	int markedPosition = -1;
	/** Stackgröße an Zielposition, -1=noch unbekannt */
	int stackSize = -1;
	/** Am Anfang true, wird von markForwardOnly() auf false gesetzt um zu signalisieren, dass Rücksprünge verboten sind */
	boolean allowJumps = true;
}
